package com.trio.livetracker.dto.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventRoot {
    private String id;
    private String type;
    private Actor actor;
    private Repo repo;
    private Payload payload;
    private LocalDateTime created_at;
}