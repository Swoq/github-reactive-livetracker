package com.trio.livetracker.controller;

import com.trio.livetracker.document.CodeUpdate;
import com.trio.livetracker.document.DocRepo;
import com.trio.livetracker.document.RepoCountAnalytic;
import com.trio.livetracker.dto.response.CodeUpdateResponse;
import com.trio.livetracker.service.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.text.ParseException;

@RestController
@RequiredArgsConstructor
public class MainController {
    private final EventService eventService;

   /* @GetMapping("/mongodb")
    public Mono<DocRepo> save() {
        return eventService.doSomething();
    }*/

    @GetMapping("/all")
    public Flux<DocRepo> findAll() {
        return eventService.findAll();
    }

    @GetMapping(value = "/updates/stream/{keyWord}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> something(@PathVariable String keyWord) {
        return eventService.getUpdates(keyWord).log("Coming on controller").map(CodeUpdateResponse::toString);
    }

    @GetMapping(value = "/topfive/{keyword}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> findTopFive(@PathVariable String keyword) {
        return eventService.findTopFiveInDay(keyword).log("Top5").map(RepoCountAnalytic::toString);
    }

    @GetMapping(value = "/languages/{keyword}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> findLanguages(@PathVariable String keyword) {
        return eventService.findLanguagesByKeyword(keyword).log("Languages").map(RepoCountAnalytic::toString);
    }
}
