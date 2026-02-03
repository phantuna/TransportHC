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
import org.example.webapplication.enums.PermissionKey;
import org.example.webapplication.enums.PermissionType;
import org.example.webapplication.exception.AppException;
import org.example.webapplication.exception.ErrorCode;
import org.example.webapplication.repository.schedule.ScheduleDocumentRepository;
import org.example.webapplication.repository.schedule.ScheduleRepository;
import org.example.webapplication.repository.user.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ScheduleService {
    private final UserRepository userRepository;
    private final ScheduleRepository scheduleRepository;
    private final ScheduleDocumentRepository scheduleDocumentRepository;
    private final PermissionService permissionService;

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

    @Transactional
    public ScheduleResponse createdSchedule (ScheduleRequest dto){
        permissionService.getUser(
                List.of(PermissionKey.CREATE),
                PermissionType.SCHEDULE
        );
        String start = dto.getStartPlace().trim();
        String end = dto.getEndPlace().trim();
        if (start.equalsIgnoreCase(end)) {
            throw new AppException(ErrorCode.START_END_PLACE_MUST_DIFFERENT);
        }
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

    public Page<ScheduleResponse> getAllSchedules(int page, int size) {
        permissionService.getUser(
                List.of(PermissionKey.VIEW,PermissionKey.MANAGE),
                PermissionType.SCHEDULE
        );
        Page<Schedule> schedulePage =
                scheduleRepository.findAll(PageRequest.of(page, size));

        return schedulePage.map(this::toResponse);
    }


    public Page<ScheduleResponse> getScheduleByUsername(int page, int size){
        permissionService.getUser(
                List.of(PermissionKey.VIEW),
                PermissionType.SCHEDULE
        );
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        Pageable pageable = PageRequest.of(page, size);
        Page<ScheduleResponse> schedulePage =
                scheduleRepository.findSchedulePageByUsername(username, pageable);

        if (schedulePage.isEmpty()) {
            throw new AppException(ErrorCode.SCHEDULE_NOT_FOUND);
        }
        List<String> scheduleIds = schedulePage.getContent().stream()
                .map(ScheduleResponse::getId)
                .toList();
        List<ScheduleDocumentResponse> documents =
                scheduleRepository.findDocumentsByScheduleIds(scheduleIds);

        Map<String, List<ScheduleDocumentResponse>> docMap =
                documents.stream()
                        .collect(Collectors.groupingBy(
                                ScheduleDocumentResponse::getScheduleId
                        ));

        schedulePage.getContent().forEach(s ->
                s.setDocuments(
                        docMap.getOrDefault(s.getId(), List.of())
                )
        );

        return schedulePage;

    }

    @Transactional
    public ScheduleResponse approvalSchedule(String id, ApprovalStatus target) {
        permissionService.getUser(
                List.of(PermissionKey.APPROVE),
                PermissionType.SCHEDULE
        );
        Schedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.SCHEDULE_NOT_FOUND));
        ApprovalStatus current = schedule.getApproval() == null ? ApprovalStatus.PENDING_APPROVAL : schedule.getApproval();
        boolean allowed;
        if (current == target) {
            throw new AppException(ErrorCode.SCHEDULE_ALREADY_IN_THIS_STATUS);
        }

        switch (current) {
            case PENDING_APPROVAL ->
                    allowed = target == ApprovalStatus.APPROVED;
            case APPROVED ->
                    allowed = target == ApprovalStatus.PENDING_APPROVAL;
            default ->
                    allowed = false;
        }
        if (!allowed) {
            throw new AppException(ErrorCode.INVALID_APPROVAL_TRANSITION);
        }
        schedule.setApproval(target);
        Schedule saved = scheduleRepository.save(schedule);
        return toResponse(saved);
    }

    @Transactional
    public ScheduleResponse updateSchedule(String scheduleId, ScheduleRequest dto){
        permissionService.getUser(
                List.of(PermissionKey.UPDATE),
                PermissionType.SCHEDULE
        );
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

    @Transactional
    public ScheduleDocumentResponse uploadDocument(String scheduleId, MultipartFile file) throws IOException {
        permissionService.getUser(
                List.of(PermissionKey.UPDATE),
                PermissionType.SCHEDULE_DOCUMENT
        );
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

    @Transactional
    public void deleteSchedule(String scheduleId) {
        permissionService.getUser(
                List.of(PermissionKey.MANAGE),
                PermissionType.SCHEDULE
        );
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new AppException(ErrorCode.SCHEDULE_NOT_FOUND));

        scheduleRepository.delete(schedule);
    }
}
