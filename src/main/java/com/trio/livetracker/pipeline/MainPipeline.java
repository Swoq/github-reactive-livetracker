package com.trio.livetracker.pipeline;

import com.trio.livetracker.document.CodeUpdate;
import com.trio.livetracker.document.DocRepo;
import com.trio.livetracker.dto.response.CodeUpdateResponse;
import com.trio.livetracker.dto.search.Item;
import com.trio.livetracker.repository.GithubRepository;
import com.trio.livetracker.request.SearchRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.Level;
import org.springframework.stereotype.Component;
import reactor.core.publisher.BufferOverflowStrategy;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuple3;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Component
@Log4j2
public class MainPipeline {
    private final GithubRepository githubRepository;
    private final SearchRequest searchRequest;
    private Flux<CodeUpdateResponse> mainFlux;
    private Set<String> keyWordSet;

    @PostConstruct
    private void postConstruct() {
        Set<String> set = new LinkedHashSet<>();
        keyWordSet = new LinkedHashSet<>();
        mainFlux = createPipeline();
    }

    private Flux<CodeUpdateResponse> createPipeline() {
        Sinks.Many<String> keyWordSink = Sinks.many().multicast().onBackpressureBuffer(10);
        Sinks.Many<Integer> searchEndedSink = Sinks.many().multicast().onBackpressureBuffer(10);
        Sinks.Many<Integer> endSavingSink = Sinks.many().multicast().onBackpressureBuffer(10);
        searchEndedSink.emitNext(0, Sinks.EmitFailureHandler.FAIL_FAST);
        endSavingSink.emitNext(0, Sinks.EmitFailureHandler.FAIL_FAST);

        Scheduler schedulers = createWorker(keyWordSink);

        return keyWordSink.asFlux()
                .onBackpressureBuffer(1000, BufferOverflowStrategy.DROP_OLDEST)
                .delayUntil(__ -> searchEndedSink.asFlux().next())
                .flatMapSequential(key -> getCodeUpdatesByKey(key, searchEndedSink, endSavingSink))
                .publishOn(schedulers)
                .publish()
                .autoConnect();
    }

    private Flux<CodeUpdateResponse> getCodeUpdatesByKey(String key, Sinks.Many<Integer> searchEndedSink, Sinks.Many<Integer> endSavingSink) {
        return searchRequest.searchCodeUpdates(key)
                .flatMapMany(searchRoot -> {
                    searchEndedSink.emitNext(0, Sinks.EmitFailureHandler.FAIL_FAST);
                    return Flux.fromIterable(searchRoot.getItems());
                })
                .doOnError(err -> log.log(Level.INFO, "Error occurred when requested for code updates. Message is: " + err.getMessage()))
                .onErrorStop()
                .buffer(20)
                .delayUntil(__ -> endSavingSink.asFlux().next())
                .flatMapSequential(Flux::fromIterable)
                .flatMapSequential(this::findCodeUpdate)
                .takeWhile(data -> data.getT2().getFullName() == null)
                .concatMap(tuple -> combineData(tuple.getT1()))
                .map(d -> processData(key, d))
                .concatMap(responseWithSaved -> responseWithSaved.map(Tuple2::getT1))
                .doOnComplete(() -> endSavingSink.emitNext(0, Sinks.EmitFailureHandler.FAIL_FAST));
    }

    private Scheduler createWorker(Sinks.Many<String> keyWordSink) {
        Scheduler schedulers = Schedulers.single();
        final int[] currKey = {0};
        schedulers.createWorker()
                .schedulePeriodically(() -> {
                    if (keyWordSet.size() > 0) {
                        keyWordSink.emitNext(keyWordSet.stream().toList().get(currKey[0]), Sinks.EmitFailureHandler.FAIL_FAST);
                        currKey[0]++;
                        if (currKey[0] == keyWordSet.size())
                            currKey[0] = 0;
                        log.log(Level.INFO, "Emit sent");
                    }
                }, 1, 30, TimeUnit.SECONDS);
        return schedulers;
    }

    private Mono<Tuple2<Item, DocRepo>> findCodeUpdate(Item item) {
        return Mono.just(item)
                .zipWith(githubRepository.findByCodeUpdateId(item.getUrl())
                        .defaultIfEmpty(new DocRepo()));
    }

    private Mono<Tuple3<Item, List<String>, DocRepo>> combineData(Item codeUpdateItem) {
        return Mono.zip(Mono.just(codeUpdateItem),
                searchRequest.searchLanguages(codeUpdateItem.getRepository().getLanguages_url()),
                githubRepository.findById(codeUpdateItem.getRepository().getFull_name())
                        .defaultIfEmpty(new DocRepo()));
    }

    private Mono<Tuple2<CodeUpdateResponse, DocRepo>> processData(String key, Tuple3<Item, List<String>, DocRepo> data) {
        var codeUpdate = CodeUpdate.builder()
                .keyWord(key)
                .url(data.getT1().getUrl())
                .build();

        var findedDocRepo = DocRepo.builder()
                .fullName(data.getT1().getRepository().getFull_name())
                .languages(data.getT2())
                .codeUpdates(List.of(codeUpdate))
                .build();

        boolean isNewRepo = true;

        if (data.getT3().getFullName() != null) {
            var updates = new ArrayList<>(data.getT3().getCodeUpdates());
            updates.add(codeUpdate);

            findedDocRepo = findedDocRepo.toBuilder()
                    .languages(data.getT2())
                    .codeUpdates(updates)
                    .build();

            isNewRepo = false;
        }

        var codeUpdateResponse = CodeUpdateResponse.builder()
                .repoFullName(data.getT1().getRepository().getFull_name())
                .codeUpdate(codeUpdate)
                .isNewRepo(isNewRepo)
                .build();

        return Mono.just(codeUpdateResponse)
                .zipWith(githubRepository.save(findedDocRepo));
    }

    private DocRepo toDocRepo(String key, Item codeUpdateInfo, List<String> languages) {
        String fullName = codeUpdateInfo.getRepository().getFull_name();
        var codeUpdate = CodeUpdate.builder()
                .keyWord(key)
                .url(codeUpdateInfo.getUrl())
                .build();

        return DocRepo.builder()
                .fullName(fullName)
                .codeUpdates(List.of(codeUpdate))
                .languages(languages)
                .build();
    }

    public boolean addKeyWord(String keyWord) {
        if (!keyWordSet.contains(keyWord))
            return keyWordSet.add(keyWord);
        return false;
    }

    public Flux<CodeUpdateResponse> getMainFlux() {
        return mainFlux;
    }
}
