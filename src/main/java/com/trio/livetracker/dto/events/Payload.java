package com.trio.livetracker.dto.events;

import java.util.ArrayList;

public class Payload{
    public Object push_id;
    public int size;
    public int distinct_size;
    public String ref;
    public String head;
    public String before;
    public ArrayList<Commit> commits;
}