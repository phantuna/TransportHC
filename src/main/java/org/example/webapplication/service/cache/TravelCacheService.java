package org.example.webapplication.service.cache;

import lombok.RequiredArgsConstructor;
import org.example.webapplication.dto.response.PageResponse;
import org.example.webapplication.dto.response.travel.TravelScheduleReportResponse;
import org.example.webapplication.repository.travel.TravelRepository;
import org.example.webapplication.repository.travel.TravelRepositoryCustom;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TravelCacheService {

    private final TravelRepository travelRepository;

    @Cacheable(
            value = "travels_list",
            key = "'page:' + #page + ':size:' + #size"
    )
    public PageResponse<TravelScheduleReportResponse> getAllTravels(int page, int size) {

        Page<TravelScheduleReportResponse> pageResult =
                travelRepository.findTravelPage(
                        PageRequest.of(page, size)
                );

        return PageResponse.<TravelScheduleReportResponse>builder()
                .content(pageResult.getContent())
                .page(page)
                .size(size)
                .totalElements(pageResult.getTotalElements())
                .totalPages(pageResult.getTotalPages())
                .build();
    }

}
