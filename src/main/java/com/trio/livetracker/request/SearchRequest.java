package com.trio.livetracker.request;

import com.trio.livetracker.dto.search.SearchRoot;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.Level;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Log4j2
public class SearchRequest {
    private final WebClient webClient;
    @Value("${github.api.url}")
    private String baseUrl;

    public Mono<SearchRoot> searchCodeUpdates(String keyword) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder.path("/search/code")
                        .queryParam("q", keyword)
                        .queryParam("sort", "indexed")
                        .queryParam("page", String.valueOf(1))
                        .build())
                .retrieve()
                .bodyToMono(SearchRoot.class)
                .retryWhen(Retry.backoff(2, Duration.ofMinutes(1))
                        .doBeforeRetry(retrySignal -> log.log(Level.INFO, "Trying to retry for the " + retrySignal.totalRetries()
                                + "time. Exception is: " + retrySignal.failure().getMessage()))
                        .maxBackoff(Duration.ofMinutes(2)))
                .doOnTerminate(() -> log.log(Level.INFO, "Terminated"));
    }

    public Mono<List<String>> searchLanguages(String repoLink) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder.path(repoLink.replace(baseUrl, ""))
                        .build())
                .retrieve()
                .bodyToMono(Map.class)
                .retryWhen(Retry.backoff(2, Duration.ofMinutes(1))
                        .maxBackoff(Duration.ofMinutes(2)))
                .map(map -> (List<String>) map.keySet().stream().collect(Collectors.toList()));
    }
}
