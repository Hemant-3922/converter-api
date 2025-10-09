package com.mnnit.controller;

import com.mnnit.model.ConversionHistory;
import com.mnnit.repository.ConversionHistoryRepository;
import org.jodconverter.core.DocumentConverter;
import org.jodconverter.core.office.OfficeException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

@RestController
@RequestMapping("/api/convert")
public class PptxPdfController {

    @Autowired
    private DocumentConverter converter; // ✅ LibreOffice/JODConverter bean

    @Autowired
    private ConversionHistoryRepository historyRepo; // ✅ Optional: save history

    // ---------- PPTX -> PDF ----------
    @PostMapping("/pptx-to-pdf")
    public ResponseEntity<byte[]> convertPptxToPdf(@RequestParam("file") MultipartFile file)
            throws IOException, OfficeException {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("No PPTX file uploaded".getBytes());
        }

        // 🔹 Save input PPTX as temp file
        File inputFile = File.createTempFile("input-", ".pptx");
        file.transferTo(inputFile);

        // 🔹 Temp output PDF
        File outputFile = File.createTempFile("output-", ".pdf");

        // 🔹 Convert PPTX → PDF
        converter.convert(inputFile).to(outputFile).execute();

        // 🔹 Read PDF bytes
        byte[] pdfBytes = Files.readAllBytes(outputFile.toPath());

        // ✅ Save conversion history
        if (historyRepo != null) {
            ConversionHistory history = new ConversionHistory(
                    file.getOriginalFilename(),
                    "PPTX → PDF",
                    "pdf",
                    100, // quality not applicable
                    pdfBytes.length
            );
            historyRepo.save(history);
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=converted.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }
}
