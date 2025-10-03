package com.mnnit.controller;

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
public class ExcelCsvPdfController {

    @Autowired
    private DocumentConverter converter; // ✅ JODConverter bean (configured with LibreOffice)

    // ---------- XLSX → PDF ----------
    @PostMapping("/xlsx-to-pdf")
    public ResponseEntity<byte[]> convertXlsxToPdf(@RequestParam("file") MultipartFile file)
            throws IOException, OfficeException {

        File inputFile = File.createTempFile("input-", ".xlsx");
        file.transferTo(inputFile);

        File outputFile = File.createTempFile("output-", ".pdf");
        converter.convert(inputFile).to(outputFile).execute();

        byte[] pdfBytes = Files.readAllBytes(outputFile.toPath());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=converted.xlsx.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    // ---------- CSV → PDF ----------
    @PostMapping("/csv-to-pdf")
    public ResponseEntity<byte[]> convertCsvToPdf(@RequestParam("file") MultipartFile file)
            throws IOException, OfficeException {

        File inputFile = File.createTempFile("input-", ".csv");
        file.transferTo(inputFile);

        File outputFile = File.createTempFile("output-", ".pdf");
        converter.convert(inputFile).to(outputFile).execute();

        byte[] pdfBytes = Files.readAllBytes(outputFile.toPath());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=converted.csv.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }
}
