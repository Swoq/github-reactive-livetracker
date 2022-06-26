package com.trio.livetracker.dto.search;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchRoot {
    private int total_count;
    private boolean incomplete_results;
    private ArrayList<Item> items;
}