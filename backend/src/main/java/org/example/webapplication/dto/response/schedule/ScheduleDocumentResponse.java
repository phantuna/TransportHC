package org.example.webapplication.dto.response.schedule;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ScheduleDocumentResponse {
    private String scheduleId;
    private String fileName;
    private String fileUrl;
    private String fileType;
    private long fileSize;


}
