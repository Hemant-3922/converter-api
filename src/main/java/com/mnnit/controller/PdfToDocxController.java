package com.mnnit.controller;

import com.mnnit.model.ConversionHistory;
import com.mnnit.repository.ConversionHistoryRepository;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@RestController
@RequestMapping("/api/convert")
public class PdfToDocxController {

    @Autowired
    private ConversionHistoryRepository historyRepo; // Optional: track conversion

    // ---------- PDF -> DOCX ----------
    @PostMapping("/pdf-to-docx")
    public ResponseEntity<byte[]> convertPdfToDocx(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("No PDF file uploaded".getBytes());
        }

        try (PDDocument document = PDDocument.load(file.getInputStream());
             XWPFDocument docx = new XWPFDocument();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);

            // Split text into paragraphs by line breaks
            for (String line : text.split("\\r?\\n")) {
                if (!line.trim().isEmpty()) {
                    XWPFParagraph para = docx.createParagraph();
                    para.createRun().setText(line);
                }
            }

            docx.write(baos);
            byte[] docxBytes = baos.toByteArray();

            // Optional: save conversion history
            if (historyRepo != null) {
                ConversionHistory history = new ConversionHistory(
                        file.getOriginalFilename(),
                        "PDF → DOCX",
                        "docx",
                        100, // quality not applicable
                        docxBytes.length
                );
                historyRepo.save(history);
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "converted.docx");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(docxBytes);

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(("Error processing PDF: " + e.getMessage()).getBytes());
        }
    }
}
