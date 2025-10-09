package com.mnnit.repository;

import com.mnnit.model.ConversionHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConversionHistoryRepository extends JpaRepository<ConversionHistory, Long> { }
