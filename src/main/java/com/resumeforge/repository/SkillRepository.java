package com.resumeforge.repository;

import com.resumeforge.entity.Skill;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SkillRepository extends JpaRepository<Skill, Long> {
    List<Skill> findByResumeIdOrderBySortOrderAsc(Long resumeId);
    void deleteByResumeId(Long resumeId);
}
