package com.mnnit.controller;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;

@RestController
@RequestMapping("/api/convert")
public class PdfToDocxController {

    // ---------- PDF -> DOCX ----------
    @PostMapping("/pdf-to-docx")
    public ResponseEntity<byte[]> convertPdfToDocx(@RequestParam("file") MultipartFile file) throws IOException {
        // Load PDF
        PDDocument document = PDDocument.load(file.getInputStream());
        PDFTextStripper stripper = new PDFTextStripper();
        String text = stripper.getText(document);
        document.close();

        // Create DOCX
        XWPFDocument docx = new XWPFDocument();
        XWPFParagraph para = docx.createParagraph();
        para.createRun().setText(text);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        docx.write(baos);
        docx.close();

        byte[] docxBytes = baos.toByteArray();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=converted.docx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(docxBytes);
    }
}
