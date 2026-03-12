package org.example.webapplication.service;

import lombok.RequiredArgsConstructor;
import org.example.webapplication.dto.request.inventory.ItemRequest;
import org.example.webapplication.dto.response.inventory.ItemResponse;
import org.example.webapplication.entity.Item;
import org.example.webapplication.exception.AppException;
import org.example.webapplication.exception.ErrorCode;
import org.example.webapplication.repository.inventory.ItemRepository;
import org.example.webapplication.service.mapper.ItemMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;
    private final ItemMapper itemMapper;

    public ItemResponse create(ItemRequest request){

        Item item = itemMapper.toEntity(request);

        Item saved = itemRepository.save(item);

        return itemMapper.toResponse(saved);
    }

    public Item updateItem(String id, String code, String name, String unit) {

        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ITEM_NOT_FOUND));

        item.setCode(code);
        item.setName(name);
        item.setUnit(unit);

        return itemRepository.save(item);
    }

    public void deleteItem(String id){

        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ITEM_NOT_FOUND));

        itemRepository.delete(item);
    }

    public Item getItemById(String id){
        return itemRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ITEM_NOT_FOUND));
    }

    public List<ItemResponse> getAll(){

        return itemRepository.findAll()
                .stream()
                .map(itemMapper::toResponse)
                .toList();
    }

}