package com.trio.livetracker.pipeline;

import com.trio.livetracker.SearchRequest;
import com.trio.livetracker.document.CodeUpdate;
import com.trio.livetracker.repository.GithubRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.Set;

@RequiredArgsConstructor
@Component

public class MainPipeline {
    private final GithubRepository githubRepository;
    private final SearchRequest searchRequest;
    private Sinks.Many<String> keyWordSink;
    private Flux<CodeUpdate> mainFlux;
    private Set<String> keyWordsSet;

    @PostConstruct
    private void postConstruct() {
        keyWordsSet = new HashSet<>();
        keyWordSink = Sinks.many().multicast().onBackpressureBuffer(10);
        mainFlux = createPipeline();
    }

    private Flux<CodeUpdate> createPipeline() {
        return Flux.range(0, 10000)
                .flatMap(d -> keyWordSink.asFlux())
                .log("Something from sink")
                .delayElements(Duration.of(5, ChronoUnit.SECONDS))
                .map(key -> Pair.of(key, searchRequest.search(key)))
                .flatMap(d -> d.getSecond().flatMapMany(s -> Flux.fromIterable(s.getItems())
                        .map(k -> Pair.of(d.getFirst(), k))))
                //.log()
                .map(item -> new CodeUpdate(item.getSecond().getSha(), item.getFirst(),  LocalDateTime.now()))
                .subscribeOn(Schedulers.single());
    }

    public boolean addKeyWord(String keyWord) {
        if (keyWordsSet.add(keyWord)) {
            keyWordSink.emitNext(keyWord, Sinks.EmitFailureHandler.FAIL_FAST);
            return true;
        }
        return false;
    }

    public Flux<CodeUpdate> getMainFlux() {
        return mainFlux;
    }
}
