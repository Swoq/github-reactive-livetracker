package com.trio.livetracker.service;

import com.trio.livetracker.document.CodeUpdate;
import com.trio.livetracker.document.DocRepo;
import com.trio.livetracker.document.RepoCountAnalytic;
import com.trio.livetracker.dto.response.CodeUpdateResponse;
import com.trio.livetracker.pipeline.MainPipeline;
import com.trio.livetracker.repository.AnalyticRepository;
import com.trio.livetracker.repository.GithubRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EventService {
    private final GithubRepository githubRepository;
    private final AnalyticRepository analyticRepository;
    private final MainPipeline mainPipeline;

    public Mono<DocRepo> doSomething() {
        CodeUpdate codeUpdate1 = CodeUpdate.builder().keyWord("keyword1").url("1").build();
        CodeUpdate codeUpdate2 = CodeUpdate.builder().keyWord("keyword2").url("2").build();
        CodeUpdate codeUpdate3 = CodeUpdate.builder().keyWord("keyword3").url("3").build();
        DocRepo docRepo = DocRepo.builder()
                .codeUpdates(List.of(codeUpdate1, codeUpdate2, codeUpdate3))
                .fullName("test")
                .languages(List.of("java"))
                .build();
        return githubRepository.save(docRepo);
    }

    public Flux<CodeUpdateResponse> getUpdates(String keyWord) {
        mainPipeline.addKeyWord(keyWord);
        return mainPipeline.getMainFlux().filter(d -> d.getCodeUpdate().getKeyWord().equals(keyWord));
    }

    public Flux<DocRepo> findAll() {
        return githubRepository.findAll()
                .log("Find")
                .filter(p -> p.getFullName().equals("gfd"));
    }

    public Flux<RepoCountAnalytic> findTopFiveInDay(String keyword) {
        Date toDate = new Date();
        Date fromDate = new Date(toDate.getTime() - 86400000);

        return analyticRepository.findTop5ByCodeUpdatesContainsKeyword(keyword, fromDate, toDate).take(5);

    }

    public Flux<RepoCountAnalytic> findLanguagesByKeyword(String keyword) {
        return analyticRepository.findByKeyword(keyword);
    }
}
