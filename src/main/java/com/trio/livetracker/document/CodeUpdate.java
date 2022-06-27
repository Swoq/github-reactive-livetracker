package com.trio.livetracker.document;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document
@EqualsAndHashCode
@ToString
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CodeUpdate {
    @Id
    private String sha;
    private String keyWord;
    private LocalDateTime timeCreated = LocalDateTime.now();
}
