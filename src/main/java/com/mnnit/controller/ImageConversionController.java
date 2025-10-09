package com.mnnit.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;

@RestController
@RequestMapping("/api/convert")
public class ImageConversionController {

    @PostMapping(value = "/image-compress", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<byte[]> convertAndCompressImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam(defaultValue = "jpg") String format,
            @RequestParam(defaultValue = "80") int quality) throws IOException {

        // Validate quality range
        if (quality < 10) quality = 10;
        if (quality > 100) quality = 100;

        // Read input image
        BufferedImage inputImage = ImageIO.read(file.getInputStream());
        if (inputImage == null) {
            throw new IOException("Invalid image file");
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        String fmt = format.toLowerCase();

        // JPEG/JPG with compression
        if (fmt.equals("jpg") || fmt.equals("jpeg")) {
            Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");
            if (!writers.hasNext()) {
                throw new IOException("No JPG writer available!");
            }
            ImageWriter writer = writers.next();

            ImageWriteParam param = writer.getDefaultWriteParam();
            if (param.canWriteCompressed()) {
                param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                param.setCompressionQuality(quality / 100f); // 0.1 to 1.0
            }

            writer.setOutput(ImageIO.createImageOutputStream(baos));
            writer.write(null, new IIOImage(inputImage, null, null), param);
            writer.dispose();
        } else {
            // PNG, GIF, BMP, etc (no adjustable compression)
            boolean success = ImageIO.write(inputImage, fmt, baos);
            if (!success) {
                throw new IOException("Unsupported image format: " + fmt);
            }
        }

        // Build response
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=converted." + fmt)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(baos.toByteArray());
    }
}
