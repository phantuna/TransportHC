package org.example.webapplication.exception;

import org.example.webapplication.dto.response.authentication.ApiResponse;
import org.example.webapplication.dto.response.ErrorItemResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice

public class GlobalExceptionHandler {
//    @ExceptionHandler(Exception.class)
//    public ResponseEntity<ApiResponse<?>> handleException(Exception e) {
//        ApiResponse<Object> response = new ApiResponse<>();
//        response.setCode(999);
//        response.setMessage(e.getMessage());
//        return ResponseEntity.internalServerError().body(response);
//    }

    @ExceptionHandler(value = AppException.class)
    ResponseEntity<ApiResponse> handlingAppException(AppException e){
        ErrorCode errorCode = e.getErrorCode();

        ApiResponse response = new ApiResponse<>();
        response.setCode(errorCode.getCode());
        response.setMessage(errorCode.getMessage());
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> handleValidation(MethodArgumentNotValidException e) {

        List<ErrorItemResponse> errors = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(err -> {
                    ErrorCode ec = ErrorCode.valueOf(err.getDefaultMessage()); // ✅ message phải là tên enum
                    return new ErrorItemResponse(ec.getCode(), ec.getMessage());
                })
                .toList();

        ApiResponse<Object> response = new ApiResponse<>();
        response.setCode(ErrorCode.VALIDATION_FAILED.getCode());
        response.setMessage(ErrorCode.VALIDATION_FAILED.getMessage());
        response.setResult(errors);

        return ResponseEntity.badRequest().body(response);
    }

}
