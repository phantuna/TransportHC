package org.example.webapplication.service.mapper;

import lombok.RequiredArgsConstructor;
import org.example.webapplication.dto.response.schedule.ScheduleDocumentResponse;
import org.example.webapplication.dto.response.schedule.ScheduleResponse;
import org.example.webapplication.entity.Schedule;
import org.example.webapplication.entity.Travel;
import org.example.webapplication.repository.travel.TravelRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ScheduleMapper {
    private final TravelRepository travelRepository;

    public ScheduleResponse toResponse(Schedule schedule) {
        String driverName = "Chưa phân công";

        Travel travel = travelRepository
                .findCurrentBySchedule(schedule.getId(), LocalDate.now());

        if (travel != null && travel.getUser() != null) {
            driverName = travel.getUser().getUsername();
        }

        List<ScheduleDocumentResponse> documents =
                schedule.getDocuments() == null
                        ? List.of()
                        : schedule.getDocuments().stream()
                        .map(doc -> ScheduleDocumentResponse.builder()
                                .fileName(doc.getFileName())
                                .fileUrl(doc.getFileUrl())
                                .fileType(doc.getFileType())
                                .fileSize(doc.getFileSize())
                                .scheduleId(schedule.getId())
                                .build())
                        .toList();

        return ScheduleResponse.builder()
                .id(schedule.getId())
                .startPlace(schedule.getStartPlace())
                .endPlace(schedule.getEndPlace())
                .expense(schedule.getExpense())
                .approval(schedule.getApproval())
                .description(schedule.getDescription())
                .driverName(driverName)
                .documents(documents)
                .build();
    }
}
