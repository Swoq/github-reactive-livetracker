package com.trio.livetracker.dto.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Commit{
    private String sha;
    private Author author;
    private String message;
    private boolean distinct;
    private String url;
}