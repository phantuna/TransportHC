package org.example.webapplication.exception;

import org.example.webapplication.dto.response.authentication.ApiResponse;
import org.example.webapplication.dto.response.ErrorItemResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.Locale;

@RestControllerAdvice

public class GlobalExceptionHandler {
//    @ExceptionHandler(Exception.class)
//    public ResponseEntity<ApiResponse<?>> handleException(Exception e) {
//        ApiResponse<Object> response = new ApiResponse<>();
//        response.setCode(999);
//        response.setMessage(e.getMessage());
//        return ResponseEntity.internalServerError().body(response);
//    }
    @Autowired
    private MessageSource messageSource;

//    @ExceptionHandler(value = AppException.class)
//    ResponseEntity<ApiResponse> handlingAppException(AppException e){
//        ErrorCode errorCode = e.getErrorCode();
//
//        ApiResponse response = new ApiResponse<>();
//        response.setCode(errorCode.getCode());
//        response.setMessage(errorCode.getMessage());
//        return ResponseEntity.badRequest().body(response);
//    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> handleValidation(MethodArgumentNotValidException e) {
        Locale locale = LocaleContextHolder.getLocale();

        List<ErrorItemResponse> errors = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(err -> {
                    String messageKey = err.getDefaultMessage(); // ví dụ: validation.phone.invalid

                    String localizedMessage = messageSource.getMessage(
                            messageKey,
                            null,
                            locale
                    );

                    return new ErrorItemResponse(
                            ErrorCode.VALIDATION_FAILED.getCode(), // hoặc null nếu muốn
                            localizedMessage
                    );
                })
                .toList();

        ApiResponse<Object> response = new ApiResponse<>();
        response.setCode(ErrorCode.VALIDATION_FAILED.getCode());
        response.setMessage(ErrorCode.VALIDATION_FAILED.getMessage());
        response.setResult(errors);

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(value = AppException.class)
    ResponseEntity<ApiResponse> handlingAppLangException(AppException exception) {
        ErrorCode errorCode = exception.getErrorCode();

        // Lấy Locale hiện tại từ Context (Spring tự động bắt từ Accept-Language header)
        Locale locale = LocaleContextHolder.getLocale();

        // Dịch tin nhắn
        String localizedMessage = messageSource.getMessage(
                errorCode.getMessage(),
                null,
                locale
        );

        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setCode(errorCode.getCode());
        apiResponse.setMessage(localizedMessage);

        return ResponseEntity.badRequest().body(apiResponse);
    }
}
