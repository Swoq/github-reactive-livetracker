package com.trio.livetracker.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.Optional;

@RestController
public class MainController {

    @GetMapping(value = "watch", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> watchKeyword(@RequestParam(required = false) Optional<String> keyword) {
        return Flux.just("1", "2", "3").delayElements(Duration.ofSeconds(1));
    }
}
