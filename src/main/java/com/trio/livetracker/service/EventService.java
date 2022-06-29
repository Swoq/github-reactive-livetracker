package com.trio.livetracker.service;

import com.trio.livetracker.document.CodeUpdate;
import com.trio.livetracker.document.DocRepo;
import com.trio.livetracker.document.LanguagesAnalytic;
import com.trio.livetracker.document.RepoCountAnalytic;
import com.trio.livetracker.pipeline.MainPipeline;
import com.trio.livetracker.repository.AnalyticRepository;
import com.trio.livetracker.repository.GithubRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mapping.model.Property;
import org.springframework.data.mongodb.core.aggregation.DateOperators;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
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
        CodeUpdate codeUpdate1 = new CodeUpdate("1", "keyword1",  LocalDateTime.now());
        CodeUpdate codeUpdate2 = new CodeUpdate("2", "keyword2",  LocalDateTime.now());
        CodeUpdate codeUpdate3 = new CodeUpdate("3", "keyword3",  LocalDateTime.now());
        DocRepo docRepo = new DocRepo("впы", List.of(codeUpdate1, codeUpdate2, codeUpdate3),List.of("java"));
        return githubRepository.save(docRepo);
    }

    public Flux<CodeUpdate> getUpdates(String keyWord) {
        mainPipeline.addKeyWord(keyWord);
        return mainPipeline.getMainFlux().filter(d -> d.getKeyWord().equals(keyWord));
    }

    public Flux<DocRepo> findAll() {
        return githubRepository.findAll()
                .log("Find")
                .filter(p -> p.getFullName().equals("gfd"));
    }

    public Flux<RepoCountAnalytic> findTopFiveInDay(String keyword) throws ParseException {
        Date toDate = new Date();
        Date fromDate = new Date(toDate.getTime() - 86400000);

        return analyticRepository.findTop5ByCodeUpdatesContainsKeyword(keyword, fromDate, toDate).take(5);

    }

    public Flux<RepoCountAnalytic> findLanguagesByKeyword(String keyword) {
        return analyticRepository.findByKeyword(keyword);
    }
}
