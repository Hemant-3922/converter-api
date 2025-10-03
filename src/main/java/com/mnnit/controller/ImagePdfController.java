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

    // ---------- JPG -> PDF ----------
    @PostMapping("/jpg-to-pdf")
    public ResponseEntity<byte[]> convertJpgToPdf(@RequestParam("files") MultipartFile[] files) {
        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            for (MultipartFile file : files) {
                BufferedImage bimg = ImageIO.read(file.getInputStream());

                // Create page with same size as image
                PDPage page = new PDPage(new PDRectangle(bimg.getWidth(), bimg.getHeight()));
                document.addPage(page);

                PDImageXObject pdImage = LosslessFactory.createFromImage(document, bimg);

                try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                    contentStream.drawImage(pdImage, 0, 0, bimg.getWidth(), bimg.getHeight());
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
            return ResponseEntity.badRequest().build();
        }
    }
}
