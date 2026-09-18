package com.resumeforge.repository;

import com.resumeforge.entity.Resume;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ResumeRepository extends JpaRepository<Resume, Long> {
    List<Resume> findByUserIdOrderByUpdatedAtDesc(Long userId);
    long countByUserId(Long userId);
}
