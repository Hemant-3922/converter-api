package com.mnnit.controller;

import org.apache.tika.Tika;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.*;

@RestController
@CrossOrigin(origins = "*")
public class FileDetectionController {

    @PostMapping("/detectFileType")
    public ResponseEntity<Map<String, Object>> detectFileType(@RequestParam("file") MultipartFile file) {
        try {
            String fileName = Objects.requireNonNull(file.getOriginalFilename());
            String extension = fileName.substring(fileName.lastIndexOf('.') + 1);
            String mimeType = file.getContentType();

            // fallback detection using Apache Tika
            if (mimeType == null || mimeType.equals("application/octet-stream")) {
                try (InputStream is = file.getInputStream()) {
                    mimeType = new Tika().detect(is, fileName);
                }
            }

            // Determine category using MIME or extension
            String category = getCategory(mimeType, extension);

            // Map category to actions
            Map<String, List<String>> actionMap = Map.of(
                    "image", List.of("Convert to PDF", "Compress", "Add Watermark"),
                    "pdf", List.of("Split", "Merge", "Encrypt", "Extract Text"),
                    "text", List.of("Convert to PDF", "Summarize"),
                    "audio", List.of("Convert to Text", "Compress Audio")
            );

            // Prepare response
            Map<String, Object> response = new HashMap<>();
            response.put("fileName", fileName);
            response.put("extension", extension);
            response.put("mimeType", mimeType);
            response.put("category", category);
            response.put("actions", actionMap.getOrDefault(category, List.of("No actions available")));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    // Determine file category based on MIME type, fallback to extension
    private String getCategory(String mimeType, String extension) {
        if (mimeType != null) {
            if (mimeType.startsWith("image")) return "image";
            if (mimeType.startsWith("text")) return "text";
            if (mimeType.equals("application/pdf")) return "pdf";
            if (mimeType.startsWith("audio")) return "audio";
        }

        // Fallback using extension
        switch (extension.toLowerCase()) {
            case "jpg", "jpeg", "png", "gif" -> { return "image"; }
            case "pdf" -> { return "pdf"; }
            case "txt", "csv", "log", "md" -> { return "text"; }
            case "mp3", "wav", "m4a" -> { return "audio"; }
            default -> { return "unknown"; }
        }
    }
}
