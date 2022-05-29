package com.trio.livetracker.dto.events;

import java.time.LocalDateTime;

public class EventRoot {
    public String id;
    public String type;
    public Actor actor;
    public Repo repo;
    public Payload payload;
    public LocalDateTime created_at;
}