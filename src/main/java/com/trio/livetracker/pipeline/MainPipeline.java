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
                                    .map(allData -> toDocRepo(key, allData.getT1(), allData.getT2()))
                                    .map(docRepo -> new CodeUpdateResponse(docRepo.getFullName(),true,docRepo.getCodeUpdates().get(0)));
                        }
                )
                .flatMap(docRepoFlux -> docRepoFlux)
                .publishOn(schedulers)
                .publish()
                .autoConnect();
    }

    private Mono<Tuple2<Item, List<String>>> combineData(Item codeUpdateItem) {
        return Mono.just(codeUpdateItem)
                .zipWith(searchRequest.searchRepo(codeUpdateItem.getRepository()
                        .getLanguages_url()));
    }

    private DocRepo toDocRepo(String key, Item codeUpdateInfo, List<String> languages) {
        String fullName = codeUpdateInfo.getRepository().getFull_name();
        CodeUpdate codeUpdate = new CodeUpdate(codeUpdateInfo.getUrl(), key, LocalDateTime.now());
        DocRepo docRepo = new DocRepo(fullName, List.of(codeUpdate), languages);
        return docRepo;
    }

    public boolean addKeyWord(String keyWord) {
        if (!keyWordsSet.contains(keyWord)) {
            keyWordsSet.add(keyWord);
            //keyWordSink.emitNext(keyWord, Sinks.EmitFailureHandler.FAIL_FAST);
            return true;
        }
        return false;
    }

    public Flux<CodeUpdateResponse> getMainFlux() {
        return mainFlux;
    }
}
