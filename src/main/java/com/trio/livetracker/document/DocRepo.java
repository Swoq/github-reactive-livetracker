package com.trio.livetracker.document;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document
@EqualsAndHashCode
@ToString
@Getter
@Setter
@AllArgsConstructor
public class DocRepo {
    @Id
    private String sha;
    private String fullName;
    private List<CodeUpdate> codeUpdates;
}
