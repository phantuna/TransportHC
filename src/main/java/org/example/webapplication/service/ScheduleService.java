package org.example.webapplication.service;


import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.webapplication.enums.ApprovalStatus;
import org.example.webapplication.dto.request.schedule.ScheduleRequest;
import org.example.webapplication.dto.response.schedule.ScheduleDocumentResponse;
import org.example.webapplication.dto.response.schedule.ScheduleResponse;
import org.example.webapplication.entity.Schedule;
import org.example.webapplication.entity.ScheduleDocument;
import org.example.webapplication.entity.User;
import org.example.webapplication.exception.AppException;
import org.example.webapplication.exception.ErrorCode;
import org.example.webapplication.repository.ScheduleDocumentRepository;
import org.example.webapplication.repository.ScheduleRepository;
import org.example.webapplication.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ScheduleService {
    private final UserRepository userRepository;
    private final ScheduleRepository scheduleRepository;
    private final ScheduleDocumentRepository scheduleDocumentRepository;
    @Value("${file.upload-dir}")
    private String uploadPath ;


    public ScheduleResponse toResponse(Schedule schedule) {

        String driverName = null;
        if (schedule.getDrivers() != null && !schedule.getDrivers().isEmpty()) {
            driverName = schedule.getDrivers().iterator().next().getUsername();
        }

        List<ScheduleDocumentResponse> documents = new ArrayList<>();
        if (schedule.getDocuments() != null) {
            for (ScheduleDocument doc : schedule.getDocuments()) {
                documents.add(
                        ScheduleDocumentResponse.builder()
                                .fileName(doc.getFileName())
                                .fileUrl(doc.getFileUrl())
                                .fileType(doc.getFileType())
                                .fileSize(doc.getFileSize())
                                .build()
                );
            }
        }

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


    @PreAuthorize("hasAuthority('CREATE_SCHEDULE')")
    public ScheduleResponse createdSchedule (ScheduleRequest dto){
        String start = dto.getStartPlace().trim();
        String end = dto.getEndPlace().trim();
        if (start.equalsIgnoreCase(end)) {
            throw new AppException(ErrorCode.START_END_PLACE_MUST_DIFFERENT);
        }

        // Check trùng tuyến
        if (scheduleRepository.existsByStartPlaceIgnoreCaseAndEndPlaceIgnoreCase(start, end)) {
            throw new AppException(ErrorCode.SCHEDULE_ROUTE_EXISTED);
        }

        if (dto.getExpense() < 0) {
            throw new AppException(ErrorCode.INVALID_EXPENSE);
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = auth.getName();

        User user = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new AppException(ErrorCode.DRIVER_NOT_FOUND));
        Schedule schedule = new Schedule ();
        schedule.setStartPlace(dto.getStartPlace());
        schedule.setEndPlace(dto.getEndPlace());
        schedule.setExpense(dto.getExpense());
        schedule.setDescription(dto.getDescription());
        schedule.setApproval(ApprovalStatus.PENDING_APPROVAL);
        schedule.getDrivers().add(user);
        Schedule saved = scheduleRepository.save(schedule);

        return toResponse(saved);
    }

    @PreAuthorize("hasAuthority('MANAGER_SCHEDULE') OR hasAuthority('VIEW_SCHEDULE')")
    public Page<ScheduleResponse> getAllSchedules(int page, int size) {

        Page<Schedule> schedulePage =
                scheduleRepository.findAll(PageRequest.of(page, size));

        return schedulePage.map(this::toResponse);
    }


    @PreAuthorize("hasAuthority('VIEW_SCHEDULE')")
    public List<ScheduleResponse> getScheduleByUsername(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = auth.getName();

        List<Schedule> schedules =
                scheduleRepository.findByDrivers_Username(currentUsername);

        if (schedules.isEmpty()) {
            throw new AppException(ErrorCode.SCHEDULE_NOT_FOUND);
        }

        List<ScheduleResponse> responses = new ArrayList<>();

        for (Schedule schedule : schedules) {

            List<ScheduleDocumentResponse> docResponses = new ArrayList<>();
            for (ScheduleDocument doc : schedule.getDocuments()) {
                ScheduleDocumentResponse r = ScheduleDocumentResponse.builder()
                        .fileName(doc.getFileName())
                        .fileUrl(doc.getFileUrl())
                        .fileType(doc.getFileType())
                        .fileSize(doc.getFileSize())
                        .build();
                docResponses.add(r);
            }

            ScheduleResponse response = ScheduleResponse.builder()
                    .id(schedule.getId())
                    .startPlace(schedule.getStartPlace())
                    .endPlace(schedule.getEndPlace())
                    .expense(schedule.getExpense())
                    .approval(schedule.getApproval())
                    .description(schedule.getDescription())
                    .documents(docResponses)
                    .driverName(currentUsername)
                    .build();

            responses.add(response);
        }

        return responses;
    }

    @PreAuthorize("hasAuthority('APPROVE_SCHEDULE')")
    public ScheduleResponse approvalSchedule(String id, ApprovalStatus target) {
        Schedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.SCHEDULE_NOT_FOUND));

        ApprovalStatus current = schedule.getApproval();
        if (current == null) current = ApprovalStatus.PENDING_APPROVAL;

        // APPROVED -> PENDING (hủy duyệt)
        if (target == ApprovalStatus.PENDING_APPROVAL) {
            if (current != ApprovalStatus.APPROVED) {
                throw new AppException(ErrorCode.INVALID_APPROVAL_TRANSITION);
            }
        }

        // PENDING -> APPROVED
        if (target == ApprovalStatus.APPROVED) {
            if (current != ApprovalStatus.PENDING_APPROVAL) {
                throw new AppException(ErrorCode.INVALID_APPROVAL_TRANSITION);
            }
        }

        schedule.setApproval(target);
        Schedule saved = scheduleRepository.save(schedule);

        return toResponse(saved);
    }

    @PreAuthorize("hasAuthority('UPDATE_SCHEDULE')")
    public ScheduleResponse updateSchedule(String scheduleId, ScheduleRequest dto){
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new AppException(ErrorCode.SCHEDULE_NOT_FOUND));
        schedule.setStartPlace(dto.getStartPlace());
        schedule.setEndPlace(dto.getEndPlace());
        schedule.setDescription(dto.getDescription());
        schedule.setExpense(dto.getExpense());

        Schedule saved = scheduleRepository.save(schedule);
        ScheduleResponse response =toResponse(saved);
        return response;
    }

    @PreAuthorize("hasAuthority('UPDATE_SCHEDULE_DOCUMENT') OR hasAuthority('MANAGE_SCHEDULE') ")
    @Transactional
    public ScheduleDocumentResponse uploadDocument(String scheduleId, MultipartFile file) throws IOException {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new AppException(ErrorCode.SCHEDULE_NOT_FOUND));

        if (file.isEmpty()) {
            throw new AppException(ErrorCode.FILE_EMPTY);
        }

        if (file.getSize() > 5 * 1024 * 1024) {
            throw new AppException(ErrorCode.FILE_TOO_LARGE);
        }

        Path uploadDir = Paths.get(uploadPath );
        Files.createDirectories(uploadDir);

        String storedName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path dest = uploadDir.resolve(storedName);
        Files.copy(file.getInputStream(), dest);

        ScheduleDocument document = new ScheduleDocument();
        document.setSchedule(schedule);
        document.setFileName(file.getOriginalFilename());
        document.setFileType(file.getContentType());
        document.setFileSize(file.getSize());
        document.setFileUrl("/files/" + storedName);

        schedule.getDocuments().add(document);

        ScheduleDocument saved = scheduleDocumentRepository.save(document);

        return ScheduleDocumentResponse.builder()
                .fileName(saved.getFileName())
                .fileSize(saved.getFileSize())
                .fileType(saved.getFileType())
                .fileUrl(saved.getFileUrl())
                .scheduleId(schedule.getId())

                .build();

    }

    @PreAuthorize("hasAuthority('MANAGER_SCHEDULE')")
    @Transactional
    public void deleteSchedule(String scheduleId) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new AppException(ErrorCode.SCHEDULE_NOT_FOUND));

        scheduleRepository.delete(schedule);
    }
}
