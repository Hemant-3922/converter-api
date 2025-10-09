package com.mnnit.controller;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@RestController
@RequestMapping("/api/convert")
public class PdfImageController {

    // ---------- PDF -> JPG (All Pages to ZIP) ----------
    @PostMapping("/pdf-to-jpg")
    public ResponseEntity<byte[]> convertPdfToJpg(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("No PDF file uploaded".getBytes());
        }

        try (PDDocument document = PDDocument.load(file.getInputStream());
             ByteArrayOutputStream zipBaos = new ByteArrayOutputStream();
             ZipOutputStream zipOut = new ZipOutputStream(zipBaos)) {

            int pageCount = document.getNumberOfPages();
            if (pageCount == 0) {
                return ResponseEntity.badRequest().body("PDF has no pages".getBytes());
            }

            PDFRenderer pdfRenderer = new PDFRenderer(document);

            for (int page = 0; page < pageCount; page++) {
                BufferedImage bim = pdfRenderer.renderImageWithDPI(page, 300); // high-quality

                try (ByteArrayOutputStream imageBaos = new ByteArrayOutputStream()) {
                    ImageIO.write(bim, "jpg", imageBaos);

                    ZipEntry entry = new ZipEntry("page-" + (page + 1) + ".jpg");
                    zipOut.putNextEntry(entry);
                    zipOut.write(imageBaos.toByteArray());
                    zipOut.closeEntry();
                }
            }

            zipOut.finish();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "converted-images.zip");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(zipBaos.toByteArray());

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(("Error processing PDF: " + e.getMessage()).getBytes());
        }
    }
}
