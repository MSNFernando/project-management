package com.springpm.springpm.api.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateWorkspaceRequest {
    private String name;
    private String slug;
    private String description;
}
