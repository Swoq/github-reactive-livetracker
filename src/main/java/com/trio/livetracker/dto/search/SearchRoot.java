package com.trio.livetracker.dto.search;

import lombok.ToString;

import java.util.ArrayList;
public class SearchRoot {
    public int total_count;
    public boolean incomplete_results;
    public ArrayList<Item> items;
}