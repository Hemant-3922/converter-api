package com.mnnit.controller;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@RestController
@RequestMapping("/api/convert")
public class ImagePdfController {

    // ---------- JPG/PNG -> PDF (Fit into A4) ----------
    @PostMapping("/jpg-to-pdf")
    public ResponseEntity<byte[]> convertJpgToPdf(
            @RequestParam(value = "files", required = false) MultipartFile[] files,
            @RequestParam(value = "file", required = false) MultipartFile[] singleFiles) {

        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            // Merge "files" and "file" into one array
            MultipartFile[] allFiles;
            if (files != null && files.length > 0) {
                allFiles = files;
            } else if (singleFiles != null && singleFiles.length > 0) {
                allFiles = singleFiles;
            } else {
                return ResponseEntity.badRequest().body("No files uploaded".getBytes());
            }

            PDRectangle pageSize = PDRectangle.A4;
            float pageWidth = pageSize.getWidth();
            float pageHeight = pageSize.getHeight();

            for (MultipartFile file : allFiles) {
                BufferedImage bimg = ImageIO.read(file.getInputStream());
                if (bimg == null) continue;

                // Scale image to fit A4 while preserving aspect ratio
                float imgWidth = bimg.getWidth();
                float imgHeight = bimg.getHeight();

                float scale = Math.min(pageWidth / imgWidth, pageHeight / imgHeight);
                float scaledWidth = imgWidth * scale;
                float scaledHeight = imgHeight * scale;

                float x = (pageWidth - scaledWidth) / 2;
                float y = (pageHeight - scaledHeight) / 2;

                PDPage page = new PDPage(pageSize);
                document.addPage(page);

                PDImageXObject pdImage = LosslessFactory.createFromImage(document, bimg);

                try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                    contentStream.drawImage(pdImage, x, y, scaledWidth, scaledHeight);
                }
            }

            // Save merged PDF
            document.save(baos);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "converted.pdf");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(baos.toByteArray());

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(("Error: " + e.getMessage()).getBytes());
        }
    }
}
