package org.example.webapplication.Dto.response.Schedule;


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
