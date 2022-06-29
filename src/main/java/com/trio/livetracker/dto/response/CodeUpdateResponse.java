package com.trio.livetracker.dto.response;

import com.trio.livetracker.document.CodeUpdate;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(toBuilder = true)
public class CodeUpdateResponse {
    private String repoFullName;
    private boolean isNewRepo;
    private CodeUpdate codeUpdate;
}
