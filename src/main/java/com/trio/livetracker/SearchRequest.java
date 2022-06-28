package com.trio.livetracker;

import com.trio.livetracker.dto.search.SearchRoot;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.Level;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

@Component
@RequiredArgsConstructor
@Log4j2
public class SearchRequest {
    private final WebClient webClient;
    @Value("${github.token}")
    private String token;

    public Mono<SearchRoot> search(String keyword) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder.queryParam("q", keyword)
                        .queryParam("sort", "indexed")
                        .queryParam("page", String.valueOf(1))

                        .build())
                .header("Authorization", "token " + token)
                .header("Accept", "application/vnd.github.v3+json")
                .header("User-Agent", "alllef")
                .retrieve()
                .bodyToMono(SearchRoot.class)
                .retryWhen(Retry.backoff(2,Duration.ofMinutes(1))
                        .maxBackoff(Duration.ofMinutes(2)))
                .doOnTerminate(() -> log.log(Level.INFO, "Terminated"));
    }
}
