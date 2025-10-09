package com.mnnit.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "conversion_history")
public class ConversionHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fileName;
    private String conversionType;
    private String outputFormat;
    private int quality;
    private long fileSize;
    private LocalDateTime timestamp;

    public ConversionHistory() {}

    public ConversionHistory(String fileName, String conversionType, String outputFormat, int quality, long fileSize) {
        this.fileName = fileName;
        this.conversionType = conversionType;
        this.outputFormat = outputFormat;
        this.quality = quality;
        this.fileSize = fileSize;
        this.timestamp = LocalDateTime.now();
    }

    // Getters & Setters
    public Long getId() { return id; }
    public String getFileName() { return fileName; }
    public String getConversionType() { return conversionType; }
    public String getOutputFormat() { return outputFormat; }
    public int getQuality() { return quality; }
    public long getFileSize() { return fileSize; }
    public LocalDateTime getTimestamp() { return timestamp; }
}
