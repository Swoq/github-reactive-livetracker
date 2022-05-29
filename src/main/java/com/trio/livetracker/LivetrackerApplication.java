package com.trio.livetracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class LivetrackerApplication {

    public static void main(String[] args) {
        SpringApplication.run(LivetrackerApplication.class, args);
    }

}
