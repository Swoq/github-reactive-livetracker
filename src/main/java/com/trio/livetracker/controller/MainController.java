package com.trio.livetracker.controller;

import com.trio.livetracker.document.CodeUpdate;
import com.trio.livetracker.document.DocRepo;
import com.trio.livetracker.service.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class MainController {
    private final EventService eventService;

    @GetMapping(value = "watch", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> watchKeyword(@RequestParam(required = false) Optional<String> keyword) {
        return Flux.just("1", "2", "3").delayElements(Duration.ofSeconds(1));
    }

    @GetMapping("/mongodb")
    public Mono<DocRepo> save() {
        return eventService.doSomething();
    }

    @GetMapping("/all")
    public Flux<DocRepo> findAll(){
        return eventService.findAll();
    }

    @GetMapping(value = "/{keyWord}",produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> something(@PathVariable String keyWord){
       return eventService.getUpdates(keyWord).log("Coming on controller").map(CodeUpdate::toString);
    }
}
