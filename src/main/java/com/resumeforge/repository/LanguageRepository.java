package com.resumeforge.repository;

import com.resumeforge.entity.Language;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LanguageRepository extends JpaRepository<Language, Long> {
    List<Language> findByResumeIdOrderBySortOrderAsc(Long resumeId);
    void deleteByResumeId(Long resumeId);
}
