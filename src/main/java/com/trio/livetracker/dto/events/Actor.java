package com.trio.livetracker.dto.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Actor{
    private int id;
    private String login;
    private String display_login;
    private String gravatar_id;
    private String url;
    private String avatar_url;
}