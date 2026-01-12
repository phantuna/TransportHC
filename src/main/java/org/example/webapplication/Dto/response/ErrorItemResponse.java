package org.example.webapplication.Dto.response;


import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ErrorItemResponse {
    private int code;
    private String message;
}