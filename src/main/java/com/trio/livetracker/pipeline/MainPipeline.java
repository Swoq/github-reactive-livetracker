package com.trio.livetracker.pipeline;

import com.trio.livetracker.document.DocRepo;
import com.trio.livetracker.dto.response.CodeUpdateResponse;
import com.trio.livetracker.dto.search.Item;
import com.trio.livetracker.request.SearchRequest;
import com.trio.livetracker.document.CodeUpdate;
import com.trio.livetracker.repository.GithubRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuple3;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

@RequiredArgsConstructor
@Component
public class MainPipeline {
    private final GithubRepository githubRepository;
    private final SearchRequest searchRequest;
    private Sinks.Many<String> keyWordSink;
    private Flux<CodeUpdateResponse> mainFlux;
    private List<String> keyWordsSet;
    private int currKey;

    @PostConstruct
    private void postConstruct() {
        keyWordsSet = new ArrayList<>();
        keyWordSink = Sinks.many().multicast().onBackpressureBuffer(10);
        mainFlux = createPipeline();
    }

    private Flux<CodeUpdateResponse> createPipeline() {
        Scheduler schedulers = Schedulers.single();
        schedulers.createWorker()
                .schedulePeriodically(() -> {
                    if (keyWordsSet.size() > 0) {
                        keyWordSink.emitNext(keyWordsSet.get(currKey), Sinks.EmitFailureHandler.FAIL_FAST);
                        currKey++;
                        if (currKey == keyWordsSet.size())
                            currKey = 0;
                    }
                }, 1, 10, TimeUnit.SECONDS);

        return keyWordSink.asFlux()
                .log("Something from sink")
                .delayElements(Duration.ofSeconds(10))
                .map(key -> {
                            return searchRequest.searchLanguages(key)
                                    .flatMapMany(searchRoot -> Flux.fromIterable(searchRoot.getItems()))
                                    .flatMap(this::combineData)
                                    .map(d -> processData(key, d))
                                    .flatMap(responseWithSaved -> responseWithSaved.map(Tuple2::getT1));
                        }
                )
                .flatMap(docRepoFlux -> docRepoFlux)
                .publishOn(schedulers)
                .publish()
                .autoConnect();
    }

    private Mono<Tuple3<Item, List<String>, DocRepo>> combineData(Item codeUpdateItem) {
        return Mono.zip(Mono.just(codeUpdateItem),
                searchRequest.searchRepo(codeUpdateItem.getRepository().getLanguages_url()),
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
        if (!keyWordsSet.contains(keyWord))
            return keyWordsSet.add(keyWord);
        return false;
    }

    public Flux<CodeUpdateResponse> getMainFlux() {
        return mainFlux;
    }
}
