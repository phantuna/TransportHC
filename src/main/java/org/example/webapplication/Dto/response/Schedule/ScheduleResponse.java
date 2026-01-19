package org.example.webapplication.Dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.webapplication.Enum.ApprovalStatus;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ScheduleResponse {
    private String id;
    private String startPlace;
    private String endPlace;
    private double expense;
    private String description;

    private List<ScheduleDocumentResponse> documents;
    private ApprovalStatus approval;
    private String driverName;

}
