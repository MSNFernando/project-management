package com.springpm.springpm.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkspaceDTO {
    private UUID id;
    private String name;
    private String slug;
    private String description;
    private UserDTO owner;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
