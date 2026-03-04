package com.springpm.springpm.api.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateProjectRequest {
    private String name;
    private String description;
    private String status;
    private String priority;
    private UUID teamLeadId;
    private LocalDate startDate;
    private LocalDate endDate;
}
