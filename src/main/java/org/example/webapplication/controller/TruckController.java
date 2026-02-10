package org.example.webapplication.controller;



import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.webapplication.dto.request.truck.TruckRequest;
import org.example.webapplication.dto.response.PageResponse;
import org.example.webapplication.dto.response.truck.TruckResponse;
import org.example.webapplication.service.TruckService;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/truck")
@RequiredArgsConstructor
public class TruckController {

    private final TruckService truckService;

    @PostMapping("/created")
    @PreAuthorize("isAuthenticated()")
    public TruckResponse CreatedTruck(@RequestBody @Valid TruckRequest request ){
        return truckService.createdTruck(request);
    }

    @PutMapping("/updated/{nameDriver}")
    @PreAuthorize("isAuthenticated()")
    public TruckResponse UpdatedTruck(@RequestBody @Valid TruckRequest request , @PathVariable("nameDriver") String name_driver){
        return truckService.updatedTruck(request,name_driver);
    }

    @GetMapping("/getAll")
    @PreAuthorize("isAuthenticated()")
    public PageResponse<TruckResponse> getALlTruck(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ){
        return truckService.getAllTrucks(page,size);
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("isAuthenticated()")
    public void deleteTruckById(@Valid @PathVariable String id){
        truckService.deleteTruck(id);
    }

}
