package com.waterdelivery.controller;

import com.waterdelivery.common.ApiResponse;
import com.waterdelivery.common.RequirePermission;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
public class FileController {

    private static final List<String> ALLOWED_EXTENSIONS = List.of(".jpg", ".jpeg", ".png", ".gif", ".webp");

    @Value("${app.upload.dir:./uploads}")
    private String uploadDir;

    @RequirePermission("product:edit")
    @PostMapping("/upload")
    public ApiResponse<List<String>> upload(@RequestParam("file") MultipartFile[] files) throws IOException {
        List<String> urls = new ArrayList<>();
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        File targetDir = new File(uploadDir, datePath);
        if (!targetDir.exists()) {
            targetDir.mkdirs();
        }
        for (MultipartFile file : files) {
            if (file.isEmpty()) {
                continue;
            }
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
            }
            if (!ALLOWED_EXTENSIONS.contains(extension)) {
                continue;
            }
            String newFilename = UUID.randomUUID().toString() + extension;
            File dest = new File(targetDir, newFilename);
            file.transferTo(dest);
            urls.add("/uploads/" + datePath + "/" + newFilename);
        }
        return ApiResponse.success(urls);
    }
}
