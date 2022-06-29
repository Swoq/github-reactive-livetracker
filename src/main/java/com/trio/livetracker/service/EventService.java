package com.trio.livetracker.service;

import com.trio.livetracker.document.CodeUpdate;
import com.trio.livetracker.document.DocRepo;
import com.trio.livetracker.dto.response.CodeUpdateResponse;
import com.trio.livetracker.pipeline.MainPipeline;
import com.trio.livetracker.repository.GithubRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.print.Doc;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EventService {
    private final GithubRepository githubRepository;
    private final MainPipeline mainPipeline;

    public Mono<DocRepo> doSomething() {
        // CodeUpdate codeUpdate = new CodeUpdate("rhdfs", "keyword",  LocalDateTime.now());
        //DocRepo docRepo = new DocRepo("впы", List.of(codeUpdate),List.of("something"));
        // return githubRepository.save(docRepo);
        return null;
    }

    public Flux<CodeUpdateResponse> getUpdates(String keyWord) {
        mainPipeline.addKeyWord(keyWord);
        return mainPipeline.getMainFlux()
                .filter(d -> d.getCodeUpdate().getKeyWord().equals(keyWord));
    }

    public Flux<DocRepo> findAll() {
        return githubRepository.findAll()
                .log("Find")
                .filter(p -> p.getFullName().equals("gfd"));
    }
}
