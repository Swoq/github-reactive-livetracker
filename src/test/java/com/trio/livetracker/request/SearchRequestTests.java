package com.trio.livetracker.request;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.test.StepVerifier;

import java.io.Serial;
import java.util.List;

@SpringBootTest
public class SearchRequestTests {
    @Autowired
    private SearchRequest searchRequest;
    @Test
    public void searchRepoTests() {
        String test = "https://api.github.com/repos/jamie-tergertson/jamie-tergertson.github.io/languages";
        List<String> result = searchRequest.searchRepo(test)
                .log()
                .block();
        Assertions.assertTrue(result.contains("HTML"));

    }
}
