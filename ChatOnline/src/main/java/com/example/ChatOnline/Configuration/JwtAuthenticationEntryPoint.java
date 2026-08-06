package com.example.ChatOnline.Configuration;

import com.example.ChatOnline.Exception.ErrorResponse;
import com.example.ChatOnline.Enum.ErrorCode;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;

public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {
    @Override //login k thanh cong
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        //UNAUTHORIZED(401, "Authentication is required !", HttpStatus.UNAUTHORIZED),
        ErrorCode errorCode = ErrorCode.UNAUTHORIZED;

        ErrorResponse errorResponse = ErrorResponse.builder()
                .code(errorCode.getCode()) //40101
                .status(errorCode.getHttpStatus().value()) // 401
                .error(errorCode.getHttpStatus().getReasonPhrase()) // Unauthorized
                .message(errorCode.getMessage()) // Authentication is required !
                .path(request.getRequestURI()) ///api/auth/user ....
                .build();
        //Chuyen doi sang kieu Json
        JsonMapper jsonMapper = new JsonMapper();
        response.getWriter().write(jsonMapper.writeValueAsString(errorResponse));
        response.flushBuffer();

        //flushBuffer gui du lieu ngay lap tuc ma khong can cho request ket thuc
        //dam bao response da dc commit truoc khi method ket thuc
    }


}
