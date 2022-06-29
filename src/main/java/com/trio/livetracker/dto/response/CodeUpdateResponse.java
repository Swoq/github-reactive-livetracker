package com.trio.livetracker.dto.response;

import com.trio.livetracker.document.CodeUpdate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CodeUpdateResponse {
    private String repoFullName;
    private boolean isNewRepo;
    private CodeUpdate codeUpdate;
}
