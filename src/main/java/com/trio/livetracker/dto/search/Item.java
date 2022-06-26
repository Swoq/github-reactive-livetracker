package com.trio.livetracker.dto.search;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
@Data
@NoArgsConstructor
@AllArgsConstructor
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