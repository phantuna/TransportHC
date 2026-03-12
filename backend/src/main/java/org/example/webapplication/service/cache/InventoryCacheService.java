package org.example.webapplication.service.cache;

import lombok.RequiredArgsConstructor;
import org.example.webapplication.dto.response.PageResponse;
import org.example.webapplication.dto.response.inventory.InventoryResponse;
import org.example.webapplication.entity.Inventory;
import org.example.webapplication.repository.inventory.InventoryRepository;
import org.example.webapplication.service.mapper.InventoryMapper;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryCacheService {

    private final InventoryRepository inventoryRepository;
    private final InventoryMapper inventoryMapper;

    @Cacheable(
            value = "inventories_list",
            key = "'page:' + #page + ':size:' + #size"
    )
    public PageResponse<InventoryResponse> getAllInventories(int page, int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Inventory> inventoryPage = inventoryRepository.findAll(pageable);

        List<InventoryResponse> content =
                inventoryPage.getContent()
                        .stream()
                        .map(inventoryMapper::toResponse)
                        .toList();

        return new PageResponse<>(
                content,
                page,
                size,
                inventoryPage.getTotalElements(),
                inventoryPage.getTotalPages()
        );
    }
}