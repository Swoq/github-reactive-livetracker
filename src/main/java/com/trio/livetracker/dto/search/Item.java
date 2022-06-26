package com.trio.livetracker.dto.search;

import lombok.ToString;

public class Item{
    public String name;
    public String path;
    public String sha;
    public String url;
    public String git_url;
    public String html_url;
    public Repository repository;
    public double score;
}