package org.example.webapplication.service.cache;

import lombok.RequiredArgsConstructor;
import org.example.webapplication.dto.response.PageResponse;
import org.example.webapplication.dto.response.schedule.ScheduleResponse;
import org.example.webapplication.repository.schedule.ScheduleRepository;
import org.example.webapplication.repository.schedule.ScheduleRepositoryCustom;
import org.example.webapplication.service.mapper.ScheduleMapper;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ScheduleCacheService {

    private final ScheduleRepository scheduleRepository;
    private final ScheduleMapper scheduleMapper;

    @Cacheable(
            value = "schedules_list",
            key = "'page:' + #page + ':size:' + #size"
    )
    public PageResponse<ScheduleResponse> getAllSchedules(int page, int size) {

        Page<ScheduleResponse> pageResult =
                scheduleRepository
                        .findAll(PageRequest.of(page, size))
                        .map(scheduleMapper::toResponse);

        return PageResponse.<ScheduleResponse>builder()
                .content(pageResult.getContent())
                .page(page)
                .size(size)
                .totalElements(pageResult.getTotalElements())
                .totalPages(pageResult.getTotalPages())
                .build();
    }

}
