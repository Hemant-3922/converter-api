package com.mnnit.service;

import com.mnnit.model.ConversionHistory;
import com.mnnit.repository.ConversionHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ConversionHistoryService {

    @Autowired
    private ConversionHistoryRepository historyRepository;

    /**
     * Save a conversion record using constructor (no setters needed)
     *
     * @param fileName       Original file name
     * @param conversionType Type of conversion (e.g., "DOCX → PDF", "Image Compression")
     * @param outputFormat   Output format (e.g., "pdf", "jpg")
     * @param quality        Quality / compression percentage (use 100 if not applicable)
     * @param fileSize       Size of converted file in bytes
     */
    public void saveHistory(String fileName, String conversionType, String outputFormat, int quality, long fileSize) {
        ConversionHistory record = new ConversionHistory(fileName, conversionType, outputFormat, quality, fileSize);
        historyRepository.save(record);
    }
}
