package com.resumeforge.repository;

import com.resumeforge.entity.SocialLink;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SocialLinkRepository extends JpaRepository<SocialLink, Long> {
    List<SocialLink> findByResumeId(Long resumeId);
    void deleteByResumeId(Long resumeId);
}
