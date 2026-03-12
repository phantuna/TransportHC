package org.example.webapplication.service.mapper;

import org.example.webapplication.dto.request.inventory.ItemRequest;
import org.example.webapplication.dto.response.inventory.ItemResponse;
import org.example.webapplication.entity.Item;
import org.springframework.stereotype.Component;

@Component
public class ItemMapper {

    public Item toEntity(ItemRequest request) {

        Item item = new Item();
        item.setCode(request.getCode());
        item.setName(request.getName());
        item.setUnit(request.getUnit());

        return item;
    }

    public ItemResponse toResponse(Item item){

        return ItemResponse.builder()
                .id(item.getId())
                .code(item.getCode())
                .name(item.getName())
                .unit(item.getUnit())
                .build();
    }

}