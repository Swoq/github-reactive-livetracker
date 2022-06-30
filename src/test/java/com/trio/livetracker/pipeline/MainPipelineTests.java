package com.trio.livetracker.pipeline;

import com.trio.livetracker.document.DocRepo;
import com.trio.livetracker.dto.search.Item;
import com.trio.livetracker.dto.search.Repository;
import com.trio.livetracker.dto.search.SearchRoot;
import com.trio.livetracker.repository.GithubRepository;
import com.trio.livetracker.request.SearchRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import javax.print.Doc;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MainPipelineTests {
    @Mock
    private GithubRepository githubRepository;
    @Mock
    private SearchRequest searchRequest;
    @InjectMocks
    private MainPipeline mainPipeline;

    @Test
    void testThatCodeUpdateIsNotDuplicated() {
        when(searchRequest.searchLanguages(anyString()).thenReturn(Mono.just(List.of())));

        ArrayList<Item> items = new ArrayList<>();
        List<String> elemNames = List.of("Delayed saving", "Not delayed saving 1", "Not delayed saving 2");
        for (String elem : elemNames) {
            DataNeeded dataNeeded = new DataNeeded(String.valueOf(elem), String.valueOf(elem), String.valueOf(elem));
            Repository repository = Repository.builder()
                    .full_name(dataNeeded.repoName())
                    .languages_url(dataNeeded.language_url())
                    .build();

            Item item = Item.builder()
                    .url(dataNeeded.url())
                    .repository(repository)
                    .build();
            items.add(item);
        }

        SearchRoot searchRoot = new SearchRoot();
        searchRoot.setItems(items);

        Map<String, Boolean> isSavedCodeUpdate = new HashMap<>();
        Map<String, Boolean> isSavedDocRepo = new HashMap<>();

        elemNames.forEach(elemName -> {
            isSavedCodeUpdate.put(elemName, false);
            isSavedDocRepo.put(elemName, false);
        });

        Function<DocRepo, Mono<DocRepo>> getDocRepoWithDelay = repo -> Mono.just(repo)
                .delayElement(Duration.ofSeconds(15))
                .doOnSuccess(docRepo -> {
                    isSavedDocRepo.put(docRepo.getFullName(), true);
                    isSavedCodeUpdate.put(docRepo.getCodeUpdates().get(0).getUrl(), true);
                });

        Function<DocRepo, Mono<DocRepo>> getDocRepoWithoutDelay = repo -> Mono.just(repo)
                .doOnSuccess(docRepo -> {
                    isSavedDocRepo.put(docRepo.getFullName(), true);
                    isSavedCodeUpdate.put(docRepo.getCodeUpdates().get(0).getUrl(), true);
                });

        DocRepo defaultDocRepo = DocRepo.builder()
                .fullName("default")
                .build();

        when(searchRequest.searchCodeUpdates(anyString()).thenReturn(searchRoot));

        when(githubRepository.findByCodeUpdateId(anyString()))
                .thenAnswer(invocationOnMock -> {
                    boolean isSaved = isSavedCodeUpdate.get(invocationOnMock.getArgument(0));
                    return isSaved ? Mono.just(defaultDocRepo) : Mono.empty();
                });

        when(githubRepository.save(any()))
                .thenAnswer(invocationOnMock -> {
                    DocRepo docRepo = invocationOnMock.getArgument(0);
                    return docRepo.getCodeUpdates().get(0).getUrl().equals("Delayed saving")
                            ? getDocRepoWithDelay.apply(docRepo) : getDocRepoWithoutDelay.apply(docRepo);
                });

        when(githubRepository.findById(anyString()))
                .thenAnswer(invocationOnMock -> {
                    boolean isSaved = isSavedDocRepo.get(invocationOnMock.getArgument(0));
                    return isSaved ? Mono.just(defaultDocRepo) : Mono.empty();
                });

        mainPipeline.addKeyWord("gachi");
        StepVerifier.create(mainPipeline.getMainFlux().map(resp->resp.getCodeUpdate().getUrl()))
                .expectNext("Delayed saving", "Not delayed saving 1", "Not delayed saving 2")
                .verifyComplete();
    }

    private record DataNeeded(String url, String repoName, String language_url) {
    }
}
