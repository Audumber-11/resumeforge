package com.resumeforge.repository;

import com.resumeforge.entity.AiAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AiAnalysisRepository extends JpaRepository<AiAnalysis, Long> {
    List<AiAnalysis> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<AiAnalysis> findByUserIdAndAnalysisTypeOrderByCreatedAtDesc(Long userId, String type);
    long countByUserId(Long userId);
}
