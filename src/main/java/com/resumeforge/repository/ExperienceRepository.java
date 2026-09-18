package com.resumeforge.repository;

import com.resumeforge.entity.Experience;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ExperienceRepository extends JpaRepository<Experience, Long> {
    List<Experience> findByResumeIdOrderBySortOrderAsc(Long resumeId);
    void deleteByResumeId(Long resumeId);
}
