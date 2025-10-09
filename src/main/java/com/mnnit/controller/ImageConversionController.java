package com.mnnit.controller;

import com.mnnit.model.ConversionHistory;
import com.mnnit.repository.ConversionHistoryRepository;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;

@RestController
@RequestMapping("/api/convert")
public class ImageConversionController {

    private final ConversionHistoryRepository historyRepo;

    public ImageConversionController(ConversionHistoryRepository historyRepo) {
        this.historyRepo = historyRepo;
    }

    @PostMapping(value = "/image-compress", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<byte[]> convertAndCompressImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam(defaultValue = "jpg") String format,
            @RequestParam(defaultValue = "80") int quality) throws IOException {

        if (quality < 10) quality = 10;
        if (quality > 100) quality = 100;

        BufferedImage inputImage = ImageIO.read(file.getInputStream());
        if (inputImage == null) throw new IOException("Invalid image file");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        String fmt = format.toLowerCase();

        if (fmt.equals("jpg") || fmt.equals("jpeg")) {
            Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");
            if (!writers.hasNext()) throw new IOException("No JPG writer available!");
            ImageWriter writer = writers.next();

            ImageWriteParam param = writer.getDefaultWriteParam();
            if (param.canWriteCompressed()) {
                param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                param.setCompressionQuality(quality / 100f);
            }

            writer.setOutput(ImageIO.createImageOutputStream(baos));
            writer.write(null, new IIOImage(inputImage, null, null), param);
            writer.dispose();
        } else {
            boolean success = ImageIO.write(inputImage, fmt, baos);
            if (!success) throw new IOException("Unsupported image format: " + fmt);
        }

        byte[] resultBytes = baos.toByteArray();

        // ✅ Save record to database
        ConversionHistory history = new ConversionHistory(
                file.getOriginalFilename(),
                "Image Conversion",
                fmt,
                quality,
                file.getSize()
        );
        historyRepo.save(history);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=converted." + fmt)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resultBytes);
    }

    // ✅ Endpoint to get all conversion history
    @GetMapping("/history")
    public ResponseEntity<?> getHistory() {
        return ResponseEntity.ok(historyRepo.findAll());
    }
}
