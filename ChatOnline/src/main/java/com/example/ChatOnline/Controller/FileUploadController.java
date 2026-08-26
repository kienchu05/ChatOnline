package com.example.ChatOnline.Controller;

import com.example.ChatOnline.DTO.Response.ApiResponse;
import com.example.ChatOnline.Service.CloudinaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class FileUploadController {

    private final CloudinaryService cloudinaryService;

    @PostMapping("/api/v1/files/upload")
    public ApiResponse<Map<String, String>> uploadFile(
            @RequestParam("file") MultipartFile file
    ) {
        try {
            // 1. Gọi Service để đẩy lên Cloudinary
            String uploadedUrl = cloudinaryService.uploadFile(file);

            // 2. Trả về đúng format JSON mà Frontend đang chờ
            return ApiResponse.<Map<String, String>>builder()
                    .code(HttpStatus.OK.value())
                    .message("Upload file thành công!")
                    .data(Map.of("url", uploadedUrl))
                    .build();

        } catch (IOException e) {
            return ApiResponse.<Map<String, String>>builder()
                    .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .message("Lỗi khi upload file: " + e.getMessage())
                    .build();
        }
    }
}
