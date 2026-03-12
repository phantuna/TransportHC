package org.example.webapplication.service.cache;

import lombok.RequiredArgsConstructor;
import org.example.webapplication.dto.response.PageResponse;
import org.example.webapplication.dto.response.truck.TruckResponse;
import org.example.webapplication.entity.Truck;
import org.example.webapplication.entity.User;
import org.example.webapplication.repository.truck.TruckRepository;
import org.example.webapplication.service.TruckService;
import org.example.webapplication.service.mapper.TruckMapper;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TruckCacheService {

    private final TruckRepository truckRepository;
    private final TruckMapper truckMapper;

    @Cacheable(
            value = "trucks_list",
            key = "'page:' + #page + ':size:' + #size"
    )
    public PageResponse<TruckResponse> getAllTrucks(int page, int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Truck> truckPage = truckRepository.findAll(pageable);

        List<TruckResponse> content = truckPage.getContent()
                .stream()
                .map(truckMapper::toResponse)
                .toList();

        return new PageResponse<>(
                content,
                page,
                size,
                truckPage.getTotalElements(),
                truckPage.getTotalPages()
        );
    }
}

