package com.trio.livetracker.pipeline;

import com.trio.livetracker.SearchRequest;
import com.trio.livetracker.document.CodeUpdate;
import com.trio.livetracker.repository.GithubRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Component
public class MainPipeline {
    private final GithubRepository githubRepository;
    private final SearchRequest searchRequest;
    private Sinks.Many<String> keyWordSink;
    private Flux<CodeUpdate> mainFlux;
    private List<String> keyWordsSet;
    private int currKey;

    @PostConstruct
    private void postConstruct() {
        keyWordsSet = new ArrayList<>();
        keyWordSink = Sinks.many().multicast().onBackpressureBuffer(10);
        mainFlux = createPipeline();
    }

    private Flux<CodeUpdate> createPipeline() {
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
               // .delayElements(Duration.ofSeconds(10))
                .map(key -> Pair.of(key, searchRequest.search(key)))
                .flatMap(d -> d.getSecond().flatMapMany(s -> Flux.fromIterable(s.getItems())
                        .map(k -> Pair.of(d.getFirst(), k))))
                //.flatMap(d-> Mono.just(d.getSecond()).zipWith(d.getSecond().getRepository().getLanguages_url()))
                .map(item -> new CodeUpdate(item.getSecond().getSha(), item.getFirst(), LocalDateTime.now()))
                .publishOn(schedulers)
                .publish()
                .autoConnect();
    }

    public boolean addKeyWord(String keyWord) {
        if (!keyWordsSet.contains(keyWord)) {
            keyWordsSet.add(keyWord);
            //keyWordSink.emitNext(keyWord, Sinks.EmitFailureHandler.FAIL_FAST);
            return true;
        }
        return false;
    }

    public Flux<CodeUpdate> getMainFlux() {
        return mainFlux;
    }
}
