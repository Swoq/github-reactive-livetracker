package com.trio.livetracker.pipeline;

import com.trio.livetracker.SearchRequest;
import com.trio.livetracker.document.CodeUpdate;
import com.trio.livetracker.repository.GithubRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.aggregation.ArrayOperators;
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
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

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
        Flux<Integer> infinite = Flux.<Integer, Integer>generate(() -> 0, (state, sink) -> {
            sink.next(state + 1);
            return 1;
        });
        Scheduler schedulers = Schedulers.single();
        schedulers.createWorker().schedulePeriodically(() -> keyWordsSet.forEach(s -> keyWordSink.emitNext(s, Sinks.EmitFailureHandler.FAIL_FAST)), 10, 10, TimeUnit.SECONDS);

        return /*infinite
                .map(d->d+1)
                .log("Infinite happened")
                .flatMap(d -> keyWordSink.asFlux().log("Created").map(t -> Pair.of(t, d)))*/
                keyWordSink.asFlux()
                        .log("Something from sink")
                        // .delayElements(Duration.of(10, ChronoUnit.SECONDS))
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
