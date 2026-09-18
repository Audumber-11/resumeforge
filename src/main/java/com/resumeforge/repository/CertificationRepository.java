package com.resumeforge.repository;

import com.resumeforge.entity.Certification;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CertificationRepository extends JpaRepository<Certification, Long> {
    List<Certification> findByResumeIdOrderBySortOrderAsc(Long resumeId);
    void deleteByResumeId(Long resumeId);
}
