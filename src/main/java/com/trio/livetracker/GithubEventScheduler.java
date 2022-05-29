package com.trio.livetracker;

import com.trio.livetracker.dto.search.SearchRoot;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@AllArgsConstructor
@Log4j2
public class GithubEventScheduler {

    private final WebClient webClient;

//    @Scheduled(fixedRate = 5000)
    public void reportCurrentTime() {
        webClient.get()
                .uri(uriBuilder -> uriBuilder.queryParam("q", "DefaultMultiplicationAlgorithm").build())
                .header("Authorization", "token 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")
                .header("Accept", "application/vnd.github.v3+json")
                .retrieve()
                .bodyToMono(SearchRoot.class)
                .subscribe(root -> root.items.forEach(item -> log.info(item.name)));

    }

}
