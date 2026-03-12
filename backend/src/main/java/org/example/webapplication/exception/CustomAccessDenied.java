package org.example.webapplication.exception;

import java.io.IOException;
import java.util.Locale;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.webapplication.dto.response.authentication.ApiResponse;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class CustomAccessDenied implements AccessDeniedHandler {
    private final MessageSource messageSource;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public CustomAccessDenied(MessageSource messageSource) {
        this.messageSource = messageSource;
    }
    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException ex
    ) throws IOException {

        Locale locale = LocaleContextHolder.getLocale();

        String localizedMessage = messageSource.getMessage(
                ErrorCode.FORBIDDEN.getMessage(), // key
                null,
                locale
        );

        ApiResponse<Object> apiResponse = new ApiResponse<>();
        apiResponse.setCode(ErrorCode.ACCESS_DENIED.getCode());
        apiResponse.setMessage(localizedMessage);

        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(
                objectMapper.writeValueAsString(apiResponse)
        );
    }
}
