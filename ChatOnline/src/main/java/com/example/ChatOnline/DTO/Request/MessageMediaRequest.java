package com.example.ChatOnline.DTO.Request;

public record MessageMediaRequest(
        String fileName,
        String fileType,
        String thumbnailUrl // Link url cua file da duoc upload
) {
}
