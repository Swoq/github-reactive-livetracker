package com.trio.livetracker;

import com.trio.livetracker.dto.search.SearchRoot;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

@Component
@RequiredArgsConstructor
@Log4j2
public class GithubEventScheduler {
    private final WebClient webClient;
    @Value("${github.token}")
    private String token;

    //@Scheduled(fixedRate = 50000)
    public void reportCurrentTime() {
        Flux.range(1, 20)
                .map(num -> webClient.get()
                        .uri(uriBuilder -> uriBuilder.queryParam("q", "access")
                                .queryParam("sort", "indexed")
                                .queryParam("page", String.valueOf(num))
                                .build())
                        .header("Authorization", "token " + token)
                        .header("Accept", "application/vnd.github.v3+json")
                        .retrieve()
                        .bodyToMono(SearchRoot.class)
                        .flatMapMany(root -> Flux.fromIterable(root.items))
                        .log()
                        .subscribe())
                .subscribe();
    }


}
