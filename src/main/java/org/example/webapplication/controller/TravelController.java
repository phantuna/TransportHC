package org.example.webapplication.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.example.webapplication.dto.request.travel.TravelRequest;
import org.example.webapplication.dto.response.travel.TravelResponse;
import org.example.webapplication.dto.response.travel.TravelScheduleReportResponse;
import org.example.webapplication.service.TravelService;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/travel")
public class TravelController {
    private final TravelService travelService;

    @PostMapping("/created")
    @PreAuthorize("hasAuthority('MANAGE_TRAVEL')")
    public TravelResponse createdTravel(@Valid @RequestBody  TravelRequest request){
        return travelService.createdTravel(request);
    }
    @GetMapping("/getById/{id}")
    @PreAuthorize("hasAuthority('VIEW_TRAVEL') or hasAuthority('MANAGE_TRAVEL')")
    public TravelResponse getTravelById( @PathVariable  String id){
        return travelService.getTravelById(id);
    }

    @GetMapping("/getAll")
    @PreAuthorize("hasAuthority('VIEW_TRAVEL') or hasAuthority('MANAGE_TRAVEL')")
    public Page<TravelScheduleReportResponse> getAllTravels(@RequestParam(defaultValue = "0") int page,
                                                            @RequestParam(defaultValue = "10") int size){
        return travelService.getALlTravels(page,size);
    }

    @PutMapping("/updated/{id}")
    @PreAuthorize("hasAuthority('MANAGE_TRAVEL')")
    public TravelResponse updatedTravel(@NotBlank @PathVariable  String id, @RequestBody TravelRequest request){
        return travelService.updateTravel(id,request);
    }

    @DeleteMapping("/deleted/{id}")
    public void deleteTravelById(@Valid @PathVariable String id){
        travelService.deleteTravel(id);
    }
}
