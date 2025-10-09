package com.mnnit.controller;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.JPEGFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/convert")
public class ImagePdfController {

    @PostMapping("/image-to-pdf")
    public ResponseEntity<byte[]> convertImageToPdf(
            @RequestParam(value = "files", required = false) MultipartFile[] files,
            @RequestParam(value = "file", required = false) MultipartFile[] singleFiles) {

        List<MultipartFile> allFiles = new ArrayList<>();
        if (files != null) allFiles.addAll(List.of(files));
        if (singleFiles != null) allFiles.addAll(List.of(singleFiles));

        if (allFiles.isEmpty()) {
            return ResponseEntity.badRequest().body("No image files uploaded.".getBytes());
        }

        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            PDRectangle pageSize = PDRectangle.A4;
            float pageWidth = pageSize.getWidth();
            float pageHeight = pageSize.getHeight();

            for (MultipartFile file : allFiles) {
                BufferedImage inputImage = ImageIO.read(file.getInputStream());
                if (inputImage == null) continue;

                // Convert to JPG (even if PNG, BMP, etc.)
                BufferedImage jpgImage = new BufferedImage(
                        inputImage.getWidth(),
                        inputImage.getHeight(),
                        BufferedImage.TYPE_INT_RGB
                );

                // Draw white background for transparency handling
                Graphics2D g2d = jpgImage.createGraphics();
                g2d.setColor(Color.WHITE);
                g2d.fillRect(0, 0, jpgImage.getWidth(), jpgImage.getHeight());
                g2d.drawImage(inputImage, 0, 0, null);
                g2d.dispose();

                // Scale to fit A4
                float imgWidth = jpgImage.getWidth();
                float imgHeight = jpgImage.getHeight();
                float scale = Math.min(pageWidth / imgWidth, pageHeight / imgHeight);
                float scaledWidth = imgWidth * scale;
                float scaledHeight = imgHeight * scale;
                float x = (pageWidth - scaledWidth) / 2;
                float y = (pageHeight - scaledHeight) / 2;

                PDPage page = new PDPage(pageSize);
                document.addPage(page);

                PDImageXObject pdImage = JPEGFactory.createFromImage(document, jpgImage);

                try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                    contentStream.drawImage(pdImage, x, y, scaledWidth, scaledHeight);
                }
            }

            document.save(baos);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "converted.pdf");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(baos.toByteArray());

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body(("Error: " + e.getMessage()).getBytes());
        }
    }
}
