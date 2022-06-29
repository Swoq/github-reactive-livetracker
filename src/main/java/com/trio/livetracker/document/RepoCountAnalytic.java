package com.trio.livetracker.document;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@EqualsAndHashCode
@ToString
@Getter
@Setter
@AllArgsConstructor
public class RepoCountAnalytic {
    @Id
    private String _id;
    private long count;
}
