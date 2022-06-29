package com.trio.livetracker.document;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document
@Data
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(toBuilder = true)
public class CodeUpdate {
    @Id
    private String url;
    private String keyWord;
    private final LocalDateTime timeCreated = LocalDateTime.now();
}
