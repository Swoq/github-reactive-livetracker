package com.trio.livetracker.reactor;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

public class ReactorTests {
    @Test
    public void testTakeWhile() {
        Flux<Integer> intFlux = Flux.range(1, 3)
                .takeWhile(el -> el == 2);
        StepVerifier.create(intFlux)
                .expectNext(1)
                .verifyComplete();
    }
}
