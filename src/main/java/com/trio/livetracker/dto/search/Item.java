package com.trio.livetracker.dto.search;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(toBuilder = true)
public class Item{
    private String name;
    private String path;
    private String sha;
    private String url;
    private String git_url;
    private String html_url;
    private Repository repository;
    private double score;
}