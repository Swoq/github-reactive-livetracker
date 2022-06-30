package com.trio.livetracker.document;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document
@Data
@NoArgsConstructor
public class CodeUpdate {
    @Id
    private String url;
    private String keyWord;
    private LocalDateTime timeCreated = LocalDateTime.now();
    @Builder(toBuilder = true)
    private CodeUpdate(String url, String keyWord) {
        this.url = url;
        this.keyWord = keyWord;
    }
}
