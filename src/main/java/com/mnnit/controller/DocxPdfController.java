package com.mnnit.controller;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.List;

@RestController
@RequestMapping("/api/convert")
public class DocxPdfController {

    // ---------- DOCX -> PDF ----------
    @PostMapping("/docx-to-pdf")
    public ResponseEntity<byte[]> convertDocxToPdf(@RequestParam("file") MultipartFile file) {
        try (InputStream fis = file.getInputStream();
             XWPFDocument docx = new XWPFDocument(fis);
             PDDocument pdfDoc = new PDDocument()) {

            PDPage page = new PDPage();
            pdfDoc.addPage(page);

            PDPageContentStream contentStream = new PDPageContentStream(pdfDoc, page);
            contentStream.setFont(PDType1Font.HELVETICA, 12);

            float margin = 50;
            float yStart = page.getMediaBox().getHeight() - margin;
            float leading = 14; // line spacing

            contentStream.beginText();
            contentStream.newLineAtOffset(margin, yStart);

            List<XWPFParagraph> paragraphs = docx.getParagraphs();
            for (XWPFParagraph para : paragraphs) {
                String text = para.getText();
                if (text != null && !text.isEmpty()) {
                    String[] lines = text.split("\n"); // handle line breaks safely
                    for (String line : lines) {
                        contentStream.showText(line);
                        contentStream.newLineAtOffset(0, -leading); // move down for next line
                    }
                }
                // extra line after each paragraph
                contentStream.newLineAtOffset(0, -leading);
            }

            contentStream.endText();
            contentStream.close();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            pdfDoc.save(baos);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "converted.pdf");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(baos.toByteArray());

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }
}
