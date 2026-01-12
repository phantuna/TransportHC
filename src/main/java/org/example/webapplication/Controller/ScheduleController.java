package org.example.webapplication.Controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.example.webapplication.Enum.ApprovalStatus;
import org.example.webapplication.Dto.request.ScheduleRequest;
import org.example.webapplication.Dto.response.ScheduleDocumentResponse;
import org.example.webapplication.Dto.response.ScheduleResponse;
import org.example.webapplication.Service.ScheduleService;
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
    @PreAuthorize("hasAuthority('CREATE_SCHEDULE')")
    public ScheduleResponse createSchedule(@Valid @RequestBody ScheduleRequest request){
        return scheduleService.createdSchedule(request);
    }

    @PutMapping("/updated/{scheduleId}")
    @PreAuthorize("hasAuthority('UPDATE_SCHEDULE')")
    public ScheduleResponse updateSchedule(@Valid @RequestBody ScheduleRequest request,@PathVariable String scheduleId){
        return scheduleService.updateSchedule(scheduleId,request);
    }

    @PostMapping("/approval/{scheduleId}")
    @PreAuthorize("hasAuthority('APPROVE_SCHEDULE')")
    public ScheduleResponse approvalSchedule (@NotBlank @PathVariable String scheduleId, @RequestParam ApprovalStatus status){
        return scheduleService.approvalSchedule(scheduleId,status);
    }

    @GetMapping("/getAll")
    @PreAuthorize("hasAuthority('MANAGER_SCHEDULE') OR hasAuthority('VIEW_SCHEDULE')")
    public List<ScheduleResponse> getAllSchedule(){
        return scheduleService.getAllSchedules();
    }

    @GetMapping("/getByUsername")
    @PreAuthorize("hasAuthority('VIEW_SCHEDULE')")
    public List<ScheduleResponse> getScheduleByUsername(){
        return scheduleService.getScheduleByUsername();
    }

    @PostMapping(
            value = "/{scheduleId}/documents",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @PreAuthorize("hasAuthority('UPDATE_SCHEDULE_DOCUMENT') OR hasAuthority('MANAGE_SCHEDULE') ")
    public ScheduleDocumentResponse upload(
            @Valid
            @PathVariable String scheduleId,
            @RequestPart("file") MultipartFile file
    ) throws IOException {
        return scheduleService.uploadDocument(scheduleId, file);
    }


    @DeleteMapping ("/deleted/{id}")
    public void deleteSchedule(@Valid @PathVariable String id){
        scheduleService.deleteSchedule(id);
    }

}


