package com.resumeforge.repository;

import com.resumeforge.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findByResumeIdOrderBySortOrderAsc(Long resumeId);
    void deleteByResumeId(Long resumeId);
}
