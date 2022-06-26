package com.trio.livetracker.dto.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Payload{
    private Object push_id;
    private int size;
    private int distinct_size;
    private String ref;
    private String head;
    private String before;
    private ArrayList<Commit> commits;
}