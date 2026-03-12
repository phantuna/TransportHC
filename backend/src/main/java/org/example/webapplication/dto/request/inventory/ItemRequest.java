package org.example.webapplication.dto.request.inventory;

import lombok.Data;

@Data
public class ItemRequest {

    private String code;
    private String name;
    private String unit;

}
