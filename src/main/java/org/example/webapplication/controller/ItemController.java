package org.example.webapplication.controller;

import lombok.RequiredArgsConstructor;
import org.example.webapplication.dto.request.inventory.ItemRequest;
import org.example.webapplication.dto.response.inventory.ItemResponse;
import org.example.webapplication.service.ItemService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/items")
public class ItemController {

    private final ItemService itemService;

    @PostMapping
    public ItemResponse create(@RequestBody ItemRequest request){
        return itemService.create(request);
    }

    @GetMapping
    public List<ItemResponse> getAll(){
        return itemService.getAll();
    }

}
