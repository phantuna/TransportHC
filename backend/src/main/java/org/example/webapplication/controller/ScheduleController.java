package org.example.webapplication.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.example.webapplication.dto.response.PageResponse;
import org.example.webapplication.enums.ApprovalStatus;
import org.example.webapplication.dto.request.schedule.ScheduleRequest;
import org.example.webapplication.dto.response.schedule.ScheduleDocumentResponse;
import org.example.webapplication.dto.response.schedule.ScheduleResponse;
import org.example.webapplication.service.ScheduleService;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/schedule")
public class ScheduleController {
    private final ScheduleService scheduleService;

    @PostMapping("/created")
    @PreAuthorize("isAuthenticated()")
    public ScheduleResponse createSchedule(@Valid @RequestBody ScheduleRequest request){
        return scheduleService.createdSchedule(request);
    }

    @PutMapping("/updated/{scheduleId}")
    @PreAuthorize("isAuthenticated()")
    public ScheduleResponse updateSchedule(@Valid @RequestBody ScheduleRequest request,@PathVariable String scheduleId){
        return scheduleService.updateSchedule(scheduleId,request);
    }

    @PostMapping("/approval/{scheduleId}")
    @PreAuthorize("isAuthenticated()")
    public ScheduleResponse approvalSchedule (@NotBlank @PathVariable String scheduleId, @RequestParam ApprovalStatus status){
        return scheduleService.approvalSchedule(scheduleId,status);
    }

    @GetMapping("/getAll")
    @PreAuthorize("isAuthenticated()")
    public PageResponse<ScheduleResponse> getAllSchedule(@RequestParam(defaultValue = "0") int page,
                                                         @RequestParam(defaultValue = "10") int size){
        return scheduleService.getAllSchedules(page,size);
    }

    @GetMapping("/getByUsername")
    @PreAuthorize("isAuthenticated()")
    public Page<ScheduleResponse> getScheduleByUsername(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ){
        return scheduleService.getScheduleByUsername(page,size);
    }

    @PostMapping(
            value = "/{scheduleId}/documents",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @PreAuthorize("isAuthenticated()")
    public ScheduleDocumentResponse upload(
            @Valid
            @PathVariable String scheduleId,
            @RequestPart("file") MultipartFile file
    ) throws IOException {
        return scheduleService.uploadDocument(scheduleId, file);
    }


    @DeleteMapping ("/deleted/{id}")
    @PreAuthorize("isAuthenticated()")
    public void deleteSchedule(@Valid @PathVariable String id){
        scheduleService.deleteSchedule(id);
    }

}


