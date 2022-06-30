package com.trio.livetracker.repository;

import com.trio.livetracker.document.CodeUpdate;
import com.trio.livetracker.document.DocRepo;
import com.trio.livetracker.document.RepoCountAnalytic;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@DataMongoTest
@ExtendWith(SpringExtension.class)
public class AnalyticRepositoryTest {

    @Autowired
    AnalyticRepository analyticRepository;

    List<DocRepo> testList = List.of(
            DocRepo.builder()
                    .codeUpdates(List.of(
                            CodeUpdate.builder().keyWord("keyword1").url("1").timeCreated(LocalDateTime.now()).build(),
                            CodeUpdate.builder().keyWord("keyword1").url("2").timeCreated(LocalDateTime.now()).build(),
                            CodeUpdate.builder().keyWord("keyword1").url("3").timeCreated(LocalDateTime.now()).build()
                    ))
                    .fullName("mustBeFirst")
                    .languages(List.of("java", "C++", "Python"))
                    .build(),
            DocRepo.builder()
                    .codeUpdates(List.of(
                            CodeUpdate.builder().keyWord("keyword1").url("1").timeCreated(LocalDateTime.now()).build(),
                            CodeUpdate.builder().keyWord("keyword1").url("2").timeCreated(LocalDateTime.now()).build(),
                            CodeUpdate.builder().keyWord("keyword2").url("3").timeCreated(LocalDateTime.now()).build()
                    ))
                    .fullName("mustBeSecond")
                    .languages(List.of("C++", "Python"))
                    .build(),
            DocRepo.builder()
                    .codeUpdates(List.of(
                            CodeUpdate.builder().keyWord("keyword1").url("1").timeCreated(LocalDateTime.now()).build(),
                            CodeUpdate.builder().keyWord("keyword2").url("2").timeCreated(LocalDateTime.now()).build(),
                            CodeUpdate.builder().keyWord("keyword2").url("3").timeCreated(LocalDateTime.now()).build()
                    ))
                    .fullName("mustBeThird")
                    .languages(List.of("C++"))
                    .build(),
            DocRepo.builder()
                    .codeUpdates(List.of(
                            CodeUpdate.builder().keyWord("keyword2").url("1").timeCreated(LocalDateTime.now()).build(),
                            CodeUpdate.builder().keyWord("keyword2").url("2").timeCreated(LocalDateTime.now()).build(),
                            CodeUpdate.builder().keyWord("keyword2").url("3").timeCreated(LocalDateTime.now()).build()
                    ))
                    .fullName("mustNotBe")
                    .languages(List.of("C++"))
                    .build()

    );

    @AfterEach
    void afterEach() {
        analyticRepository.deleteAll().block();
    }

    @BeforeEach
    void beforeEach() {
        analyticRepository.saveAll(testList).collectList().block();
    }

    @Test
    public void docRepoIsSaved() {
        // given
        DocRepo docRepo = new DocRepo();
        docRepo.setFullName("testUser/TestRepo");

        // when
        Mono<DocRepo> save = analyticRepository.save(docRepo);
        Mono<DocRepo> then = save.then(analyticRepository.findById("testUser/TestRepo"));

        // then
        StepVerifier.create(then)
                .expectNext(docRepo)
                .expectComplete()
                .verify();
    }

    @Test
    public void topFiveReposIsFound() {
        // given

        Flux<DocRepo> savedAll = analyticRepository.saveAll(testList);
        Date toDate = new Date();
        Date fromDate = new Date(toDate.getTime() - 86400000);

        // when
        Flux<RepoCountAnalytic> foundFive =
                savedAll.thenMany(analyticRepository.findTop5ByCodeUpdatesContainsKeyword("keyword1", fromDate, toDate));

        // then
        StepVerifier.create(foundFive)
                .expectNext(new RepoCountAnalytic(testList.get(0).getFullName(), 3))
                .expectNext(new RepoCountAnalytic(testList.get(1).getFullName(), 2))
                .expectNext(new RepoCountAnalytic(testList.get(2).getFullName(), 1))
                .expectComplete()
                .verify();

    }

    @Test
    public void returnsLanguagesByKeyword() {
        // given

        Flux<DocRepo> savedAll = analyticRepository.saveAll(testList);

        // when
        Flux<RepoCountAnalytic> languages = savedAll.thenMany(analyticRepository.findByKeyword("keyword1"));
        // then
        StepVerifier.create(languages)
                .expectNext(new RepoCountAnalytic("C++", 3))
                .expectNext(new RepoCountAnalytic("Python", 2))
                .expectNext(new RepoCountAnalytic("java", 1))
                .expectComplete()
                .verify();

    }


}
