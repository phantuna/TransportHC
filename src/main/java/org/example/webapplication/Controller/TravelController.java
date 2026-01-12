package org.example.webapplication.Controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.example.webapplication.Dto.request.TravelRequest;
import org.example.webapplication.Dto.response.TravelResponse;
import org.example.webapplication.Service.TravelService;
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
    public List<TravelResponse> getAllTravels(){
        return travelService.getALlTravels();
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
