package org.example.webapplication.Controller;



import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.webapplication.Dto.request.TruckRequest;
import org.example.webapplication.Dto.response.TruckResponse;
import org.example.webapplication.Service.TruckService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/truck")
@RequiredArgsConstructor
public class TruckController {

    private final TruckService truckService;

    @PostMapping("/created")
    @PreAuthorize("hasAuthority('MANAGE_TRUCK')")
    public TruckResponse CreatedTruck(@RequestBody @Valid TruckRequest request ){
        return truckService.createdTruck(request);
    }

    @PutMapping("/updated/{nameDriver}")
    @PreAuthorize("hasAuthority('MANAGE_TRUCK')")
    public TruckResponse UpdatedTruck(@RequestBody @Valid TruckRequest request , @PathVariable("nameDriver") String name_driver){
        return truckService.updatedTruck(request,name_driver);
    }

    @GetMapping("/getAll")
    @PreAuthorize("hasAuthority('MANAGE_TRUCK')")
    public List<TruckResponse> getALlTruck(){
        return truckService.getAllTrucks();
    }

    @DeleteMapping("/delete/{id}")
    public void deleteTruckById(@Valid @PathVariable String id){
        truckService.deleteTruck(id);
    }

}
