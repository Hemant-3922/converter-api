package com.mnnit.controller;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDPageContentStream.AppendMode;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.rendering.PDFRenderer;
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

    @PostMapping("/pdf-reduce")
    public ResponseEntity<byte[]> compressPdf(
            @RequestParam("file") MultipartFile file,
            @RequestParam(defaultValue = "medium") String level) throws IOException {

        PDDocument document = PDDocument.load(file.getInputStream());
        PDFRenderer pdfRenderer = new PDFRenderer(document);

        PDDocument compressedDoc = new PDDocument();

        int dpi;
        float jpegQuality;
        switch (level.toLowerCase()) {
            case "extreme":
                dpi = 72;          // lowest quality
                jpegQuality = 0.3f;
                break;
            case "low":
                dpi = 200;        // decent quality
                jpegQuality = 0.7f;
                break;
            default: // medium
                dpi = 120;
                jpegQuality = 0.5f;
                break;
        }

        for (int page = 0; page < document.getNumberOfPages(); page++) {
            BufferedImage bim = pdfRenderer.renderImageWithDPI(page, dpi);
            PDPage newPage = new PDPage(PDRectangle.A4);
            compressedDoc.addPage(newPage);

            byte[] imgBytes = toCompressedJpeg(bim, jpegQuality);
            PDImageXObject pdImage = PDImageXObject.createFromByteArray(compressedDoc, imgBytes, "img");

            try (PDPageContentStream contentStream = new PDPageContentStream(compressedDoc, newPage, AppendMode.OVERWRITE, true)) {
                contentStream.drawImage(pdImage, 0, 0, PDRectangle.A4.getWidth(), PDRectangle.A4.getHeight());
            }
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        compressedDoc.save(baos);
        compressedDoc.close();
        document.close();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=reduced.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(baos.toByteArray());
    }

    // ✅ Fixed Helper Method: Convert BufferedImage → Compressed JPG bytes
    private byte[] toCompressedJpeg(BufferedImage image, float quality) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");
        if (!writers.hasNext()) throw new IllegalStateException("No JPG writers available");

        ImageWriter writer = writers.next();
        ImageWriteParam param = writer.getDefaultWriteParam();
        param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        param.setCompressionQuality(quality);

        try (ImageOutputStream ios = ImageIO.createImageOutputStream(baos)) {
            writer.setOutput(ios);
            writer.write(null, new IIOImage(image, null, null), param);
        }
        writer.dispose();

        return baos.toByteArray();
    }
}
