package com.mnnit.controller;

import com.mnnit.service.ConversionHistoryService;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;

@RestController
@RequestMapping("/api/compress")
public class FileSizeReducerController {

    @Autowired
    private ConversionHistoryService historyService; // ✅ Inject history service

    @PostMapping("/pdf-reduce")
    public ResponseEntity<byte[]> compressPdf(
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) Integer compression, // 10-100%
            @RequestParam(defaultValue = "medium") String level     // low/medium/extreme
    ) throws IOException {

        // Determine compression quality (0.1 - 1.0)
        float quality;
        if (compression != null) {
            compression = Math.max(10, Math.min(compression, 100));
            quality = compression / 100f;
        } else {
            switch (level.toLowerCase()) {
                case "extreme":
                    quality = 0.3f;
                    break;
                case "low":
                    quality = 0.7f;
                    break;
                default:
                    quality = 0.5f;
            }
        }

        // Load PDF
        PDDocument document = PDDocument.load(file.getInputStream());

        // Compress only images inside PDF
        for (PDPage page : document.getPages()) {
            page.getResources().getXObjectNames().forEach(name -> {
                try {
                    if (page.getResources().isImageXObject(name)) {
                        PDImageXObject imageXObject = (PDImageXObject) page.getResources().getXObject(name);
                        BufferedImage image = imageXObject.getImage();

                        // Recompress image as JPEG
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");
                        if (!writers.hasNext()) return;
                        ImageWriter writer = writers.next();

                        ImageWriteParam param = writer.getDefaultWriteParam();
                        param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                        param.setCompressionQuality(quality);

                        try (ImageOutputStream ios = ImageIO.createImageOutputStream(baos)) {
                            writer.setOutput(ios);
                            writer.write(null, new IIOImage(image, null, null), param);
                        }
                        writer.dispose();

                        // Replace image in PDF with compressed version
                        PDImageXObject compressed = PDImageXObject.createFromByteArray(document, baos.toByteArray(), name.getName());
                        page.getResources().put(name, compressed);
                    }
                } catch (Exception ignored) {}
            });
        }

        // Save compressed PDF to byte array
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        document.save(output);
        document.close();

        byte[] compressedBytes = output.toByteArray();

        // ✅ Save history
        historyService.saveHistory(
                file.getOriginalFilename(),
                "PDF Compression",
                "pdf",
                Math.round(quality * 100),
                compressedBytes.length
        );

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=reduced-" + file.getOriginalFilename())
                .contentType(MediaType.APPLICATION_PDF)
                .body(compressedBytes);
    }
}
