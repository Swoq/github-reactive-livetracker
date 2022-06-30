package com.trio.livetracker.document;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.Objects;

@Document
@Data
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(toBuilder = true)
public class DocRepo {
    @Id
    private String fullName;
    private List<CodeUpdate> codeUpdates;
    private List<String> languages;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DocRepo)) return false;

        DocRepo docRepo = (DocRepo) o;

        return Objects.equals(fullName, docRepo.fullName);
    }

    @Override
    public int hashCode() {
        return fullName != null ? fullName.hashCode() : 0;
    }
}
