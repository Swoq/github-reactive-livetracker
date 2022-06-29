package com.trio.livetracker.dto.search;

import lombok.*;

import java.util.ArrayList;
@Data
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(toBuilder = true)
public class SearchRoot {
    private int total_count;
    private boolean incomplete_results;
    private ArrayList<Item> items;
}