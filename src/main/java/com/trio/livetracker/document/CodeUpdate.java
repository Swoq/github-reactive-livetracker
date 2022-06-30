package com.trio.livetracker.document;

import com.mongodb.lang.NonNull;
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

    @NonNull
    private LocalDateTime timeCreated = LocalDateTime.now();
}
