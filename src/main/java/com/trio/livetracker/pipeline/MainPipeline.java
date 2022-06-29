package com.trio.livetracker.pipeline;

import com.trio.livetracker.document.DocRepo;
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
    private Flux<DocRepo> mainFlux;
    private List<String> keyWordsSet;
    private int currKey;

    @PostConstruct
    private void postConstruct() {
        keyWordsSet = new ArrayList<>();
        keyWordSink = Sinks.many().multicast().onBackpressureBuffer(10);
        mainFlux = createPipeline();
    }

    private Flux<DocRepo> createPipeline() {
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
                                    .flatMap(item -> Mono.just(item).zipWith(searchRequest.searchRepo(item.getRepository().getLanguages_url())))
                                    .map(allData -> toDocRepo(key,allData.getT1(),allData.getT2()));
                        }
                )
                .flatMap(docRepoFlux -> docRepoFlux)
                .publishOn(schedulers)
                .publish()
                .autoConnect();

        //  .flatMap(d -> d.getSecond().flatMapMany(s -> Flux.fromIterable(s.getItems())
        //        .map(k -> Pair.of(d.getFirst(), k))))
               /* .flatMap(d -> Mono.just(d.getSecond()).zipWith(d.getSecond().getRepository().getLanguages_url()))
                .map(item -> new CodeUpdate(item.getSecond().getSha(), item.getFirst(), LocalDateTime.now()))
                .publishOn(schedulers)
                .publish()
                .autoConnect();
*/
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

    public Flux<DocRepo> getMainFlux() {
        return mainFlux;
    }
}
