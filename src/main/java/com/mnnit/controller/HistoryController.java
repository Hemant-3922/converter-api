package com.mnnit.controller;

import com.mnnit.model.ConversionHistory;
import com.mnnit.repository.ConversionHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/history")
public class HistoryController {

    @Autowired
    private ConversionHistoryRepository historyRepository;

    // ✅ Get all conversion history
    @GetMapping("/all")
    public ResponseEntity<List<ConversionHistory>> getAllHistory() {
        List<ConversionHistory> historyList = historyRepository.findAll();
        return ResponseEntity.ok(historyList);
    }

    // Optional: get history by type (image/pdf/etc)
    @GetMapping("/type/{type}")
    public ResponseEntity<List<ConversionHistory>> getHistoryByType(@PathVariable String type) {
        List<ConversionHistory> filtered = historyRepository.findAll()
                .stream()
                .filter(h -> h.getConversionType().equalsIgnoreCase(type))
                .toList();
        return ResponseEntity.ok(filtered);
    }
}
