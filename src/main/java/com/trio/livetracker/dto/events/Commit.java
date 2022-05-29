package com.trio.livetracker.dto.events;

public class Commit{
    public String sha;
    public Author author;
    public String message;
    public boolean distinct;
    public String url;
}