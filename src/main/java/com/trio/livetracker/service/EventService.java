package com.trio.livetracker.service;

import com.trio.livetracker.document.CodeUpdate;
import com.trio.livetracker.document.DocRepo;
import com.trio.livetracker.repository.GithubRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class EventService {
    private final GithubRepository githubRepository;

    public Mono<DocRepo> doSomething() {
        CodeUpdate codeUpdate = new CodeUpdate("rhdfs", "gachi");
        DocRepo docRepo = new DocRepo("впы", "gfd", List.of(codeUpdate));
        return githubRepository.save(docRepo);
    }

    @PostConstruct
    private void deleteAll(){
        githubRepository.deleteAll()
                .block();
    }
}
