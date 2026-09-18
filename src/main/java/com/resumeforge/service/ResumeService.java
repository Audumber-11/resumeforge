package com.resumeforge.service;

import com.resumeforge.entity.*;
import com.resumeforge.exception.ResumeNotFoundException;
import com.resumeforge.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class ResumeService {

    private final ResumeRepository resumeRepository;
    private final EducationRepository educationRepository;
    private final ExperienceRepository experienceRepository;
    private final SkillRepository skillRepository;
    private final ProjectRepository projectRepository;
    private final CertificationRepository certificationRepository;
    private final AchievementRepository achievementRepository;
    private final LanguageRepository languageRepository;
    private final SocialLinkRepository socialLinkRepository;

    public ResumeService(ResumeRepository resumeRepository,
                         EducationRepository educationRepository,
                         ExperienceRepository experienceRepository,
                         SkillRepository skillRepository,
                         ProjectRepository projectRepository,
                         CertificationRepository certificationRepository,
                         AchievementRepository achievementRepository,
                         LanguageRepository languageRepository,
                         SocialLinkRepository socialLinkRepository) {
        this.resumeRepository = resumeRepository;
        this.educationRepository = educationRepository;
        this.experienceRepository = experienceRepository;
        this.skillRepository = skillRepository;
        this.projectRepository = projectRepository;
        this.certificationRepository = certificationRepository;
        this.achievementRepository = achievementRepository;
        this.languageRepository = languageRepository;
        this.socialLinkRepository = socialLinkRepository;
    }

    // ── Social Links ──────────────────────────────────────

    public List<SocialLink> getSocialLinks(Long resumeId) {
        return socialLinkRepository.findByResumeId(resumeId);
    }

    public SocialLink addSocialLink(Long resumeId, SocialLink link) {
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new ResumeNotFoundException("Resume not found: " + resumeId));
        link.setResume(resume);
        return socialLinkRepository.save(link);
    }

    public SocialLink updateSocialLink(Long id, SocialLink updated) {
        SocialLink existing = socialLinkRepository.findById(id)
                .orElseThrow(() -> new ResumeNotFoundException("SocialLink not found: " + id));
        existing.setPlatform(updated.getPlatform());
        existing.setUrl(updated.getUrl());
        return socialLinkRepository.save(existing);
    }

    public void deleteSocialLink(Long id) {
        socialLinkRepository.deleteById(id);
    }

    // ── Resume CRUD ─────────────────────────────────────────

    public Resume createResume(Long userId, String title, User user) {
        Resume resume = new Resume(title, user);
        resume.setUser(user);
        return resumeRepository.save(resume);
    }

    @Transactional(readOnly = true)
    public Resume getResume(Long id) {
        return resumeRepository.findById(id)
                .orElseThrow(() -> new ResumeNotFoundException("Resume not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public List<Resume> getUserResumes(Long userId) {
        return resumeRepository.findByUserIdOrderByUpdatedAtDesc(userId);
    }

    @Transactional(readOnly = true)
    public long countResumes(Long userId) {
        return resumeRepository.countByUserId(userId);
    }

    public Resume updateResume(Long id, Resume updated) {
        Resume existing = getResume(id);
        existing.setTitle(updated.getTitle());
        existing.setTemplateId(updated.getTemplateId());
        existing.setPrimaryColor(updated.getPrimaryColor());
        existing.setFontFamily(updated.getFontFamily());
        existing.setFontSize(updated.getFontSize());
        existing.setSectionSpacing(updated.getSectionSpacing());
        existing.setLineSpacing(updated.getLineSpacing());
        existing.setPageMargin(updated.getPageMargin());
        existing.setSectionOrder(updated.getSectionOrder());
        existing.setFirstName(updated.getFirstName());
        existing.setLastName(updated.getLastName());
        existing.setProfessionalTitle(updated.getProfessionalTitle());
        existing.setPersonalEmail(updated.getPersonalEmail());
        existing.setPhone(updated.getPhone());
        existing.setLocation(updated.getLocation());
        existing.setWebsite(updated.getWebsite());
        existing.setLinkedin(updated.getLinkedin());
        existing.setGithub(updated.getGithub());
        existing.setProfilePhoto(updated.getProfilePhoto());
        existing.setSummary(updated.getSummary());
        return resumeRepository.save(existing);
    }

    public void deleteResume(Long id) {
        Resume resume = getResume(id);
        resumeRepository.delete(resume);
    }

    public Resume duplicateResume(Long id) {
        Resume original = getResume(id);
        Resume copy = new Resume(original.getTitle() + " (Copy)", original.getUser());
        copy.setUser(original.getUser());
        copy.setTemplateId(original.getTemplateId());
        copy.setPrimaryColor(original.getPrimaryColor());
        copy.setFontFamily(original.getFontFamily());
        copy.setFontSize(original.getFontSize());
        copy.setSectionSpacing(original.getSectionSpacing());
        copy.setLineSpacing(original.getLineSpacing());
        copy.setPageMargin(original.getPageMargin());
        copy.setSectionOrder(original.getSectionOrder());
        copy.setFirstName(original.getFirstName());
        copy.setLastName(original.getLastName());
        copy.setProfessionalTitle(original.getProfessionalTitle());
        copy.setPersonalEmail(original.getPersonalEmail());
        copy.setPhone(original.getPhone());
        copy.setLocation(original.getLocation());
        copy.setWebsite(original.getWebsite());
        copy.setLinkedin(original.getLinkedin());
        copy.setGithub(original.getGithub());
        copy.setSummary(original.getSummary());
        copy.setIsPublic(false);

        Resume savedCopy = resumeRepository.save(copy);

        // Duplicate all section items
        for (Education e : original.getEducationList()) {
            Education newE = new Education(savedCopy);
            newE.setInstitution(e.getInstitution());
            newE.setDegree(e.getDegree());
            newE.setField(e.getField());
            newE.setStartDate(e.getStartDate());
            newE.setEndDate(e.getEndDate());
            newE.setGrade(e.getGrade());
            newE.setDescription(e.getDescription());
            newE.setSortOrder(e.getSortOrder());
            educationRepository.save(newE);
        }

        for (Experience e : original.getExperienceList()) {
            Experience newE = new Experience(savedCopy);
            newE.setJobTitle(e.getJobTitle());
            newE.setCompany(e.getCompany());
            newE.setLocation(e.getLocation());
            newE.setEmploymentType(e.getEmploymentType());
            newE.setStartDate(e.getStartDate());
            newE.setEndDate(e.getEndDate());
            newE.setCurrentlyWorking(e.getCurrentlyWorking());
            newE.setDescription(e.getDescription());
            newE.setAchievements(e.getAchievements());
            newE.setSortOrder(e.getSortOrder());
            experienceRepository.save(newE);
        }

        for (Skill s : original.getSkills()) {
            Skill newS = new Skill(savedCopy);
            newS.setName(s.getName());
            newS.setCategory(s.getCategory());
            newS.setProficiency(s.getProficiency());
            newS.setSortOrder(s.getSortOrder());
            skillRepository.save(newS);
        }

        for (Project p : original.getProjects()) {
            Project newP = new Project(savedCopy);
            newP.setName(p.getName());
            newP.setDescription(p.getDescription());
            newP.setTechnologies(p.getTechnologies());
            newP.setRole(p.getRole());
            newP.setStartDate(p.getStartDate());
            newP.setEndDate(p.getEndDate());
            newP.setKeyContributions(p.getKeyContributions());
            newP.setGithubUrl(p.getGithubUrl());
            newP.setLiveDemoUrl(p.getLiveDemoUrl());
            newP.setSortOrder(p.getSortOrder());
            projectRepository.save(newP);
        }

        for (Certification c : original.getCertifications()) {
            Certification newC = new Certification(savedCopy);
            newC.setName(c.getName());
            newC.setIssuingOrganization(c.getIssuingOrganization());
            newC.setDate(c.getDate());
            newC.setCredentialId(c.getCredentialId());
            newC.setCredentialUrl(c.getCredentialUrl());
            newC.setSortOrder(c.getSortOrder());
            certificationRepository.save(newC);
        }

        for (Achievement a : original.getAchievements()) {
            Achievement newA = new Achievement(savedCopy);
            newA.setTitle(a.getTitle());
            newA.setDescription(a.getDescription());
            newA.setDate(a.getDate());
            newA.setSortOrder(a.getSortOrder());
            achievementRepository.save(newA);
        }

        for (Language l : original.getLanguages()) {
            Language newL = new Language(savedCopy);
            newL.setName(l.getName());
            newL.setProficiency(l.getProficiency());
            newL.setSortOrder(l.getSortOrder());
            languageRepository.save(newL);
        }

        return savedCopy;
    }

    // ── Section Management ──────────────────────────────────

    public Education addEducation(Long resumeId, Education education) {
        Resume resume = getResume(resumeId);
        education.setResume(resume);
        return educationRepository.save(education);
    }

    public List<Education> getEducationList(Long resumeId) {
        return educationRepository.findByResumeIdOrderBySortOrderAsc(resumeId);
    }

    public Education updateEducation(Long id, Education updated) {
        Education existing = educationRepository.findById(id)
                .orElseThrow(() -> new ResumeNotFoundException("Education not found"));
        existing.setInstitution(updated.getInstitution());
        existing.setDegree(updated.getDegree());
        existing.setField(updated.getField());
        existing.setStartDate(updated.getStartDate());
        existing.setEndDate(updated.getEndDate());
        existing.setGrade(updated.getGrade());
        existing.setDescription(updated.getDescription());
        existing.setSortOrder(updated.getSortOrder());
        return educationRepository.save(existing);
    }

    public void deleteEducation(Long id) {
        educationRepository.deleteById(id);
    }

    public Experience addExperience(Long resumeId, Experience experience) {
        Resume resume = getResume(resumeId);
        experience.setResume(resume);
        return experienceRepository.save(experience);
    }

    public List<Experience> getExperienceList(Long resumeId) {
        return experienceRepository.findByResumeIdOrderBySortOrderAsc(resumeId);
    }

    public Experience updateExperience(Long id, Experience updated) {
        Experience existing = experienceRepository.findById(id)
                .orElseThrow(() -> new ResumeNotFoundException("Experience not found"));
        existing.setJobTitle(updated.getJobTitle());
        existing.setCompany(updated.getCompany());
        existing.setLocation(updated.getLocation());
        existing.setEmploymentType(updated.getEmploymentType());
        existing.setStartDate(updated.getStartDate());
        existing.setEndDate(updated.getEndDate());
        existing.setCurrentlyWorking(updated.getCurrentlyWorking());
        existing.setDescription(updated.getDescription());
        existing.setAchievements(updated.getAchievements());
        existing.setSortOrder(updated.getSortOrder());
        return experienceRepository.save(existing);
    }

    public void deleteExperience(Long id) {
        experienceRepository.deleteById(id);
    }

    public Skill addSkill(Long resumeId, Skill skill) {
        Resume resume = getResume(resumeId);
        skill.setResume(resume);
        return skillRepository.save(skill);
    }

    public List<Skill> getSkills(Long resumeId) {
        return skillRepository.findByResumeIdOrderBySortOrderAsc(resumeId);
    }

    public Skill updateSkill(Long id, Skill updated) {
        Skill existing = skillRepository.findById(id)
                .orElseThrow(() -> new ResumeNotFoundException("Skill not found"));
        existing.setName(updated.getName());
        existing.setCategory(updated.getCategory());
        existing.setProficiency(updated.getProficiency());
        existing.setSortOrder(updated.getSortOrder());
        return skillRepository.save(existing);
    }

    public void deleteSkill(Long id) {
        skillRepository.deleteById(id);
    }

    public Project addProject(Long resumeId, Project project) {
        Resume resume = getResume(resumeId);
        project.setResume(resume);
        return projectRepository.save(project);
    }

    public List<Project> getProjects(Long resumeId) {
        return projectRepository.findByResumeIdOrderBySortOrderAsc(resumeId);
    }

    public Project updateProject(Long id, Project updated) {
        Project existing = projectRepository.findById(id)
                .orElseThrow(() -> new ResumeNotFoundException("Project not found"));
        existing.setName(updated.getName());
        existing.setDescription(updated.getDescription());
        existing.setTechnologies(updated.getTechnologies());
        existing.setRole(updated.getRole());
        existing.setStartDate(updated.getStartDate());
        existing.setEndDate(updated.getEndDate());
        existing.setKeyContributions(updated.getKeyContributions());
        existing.setGithubUrl(updated.getGithubUrl());
        existing.setLiveDemoUrl(updated.getLiveDemoUrl());
        existing.setSortOrder(updated.getSortOrder());
        return projectRepository.save(existing);
    }

    public void deleteProject(Long id) {
        projectRepository.deleteById(id);
    }

    public Certification addCertification(Long resumeId, Certification cert) {
        Resume resume = getResume(resumeId);
        cert.setResume(resume);
        return certificationRepository.save(cert);
    }

    public List<Certification> getCertifications(Long resumeId) {
        return certificationRepository.findByResumeIdOrderBySortOrderAsc(resumeId);
    }

    public Certification updateCertification(Long id, Certification updated) {
        Certification existing = certificationRepository.findById(id)
                .orElseThrow(() -> new ResumeNotFoundException("Certification not found"));
        existing.setName(updated.getName());
        existing.setIssuingOrganization(updated.getIssuingOrganization());
        existing.setDate(updated.getDate());
        existing.setCredentialId(updated.getCredentialId());
        existing.setCredentialUrl(updated.getCredentialUrl());
        existing.setSortOrder(updated.getSortOrder());
        return certificationRepository.save(existing);
    }

    public void deleteCertification(Long id) {
        certificationRepository.deleteById(id);
    }

    public Achievement addAchievement(Long resumeId, Achievement achievement) {
        Resume resume = getResume(resumeId);
        achievement.setResume(resume);
        return achievementRepository.save(achievement);
    }

    public List<Achievement> getAchievements(Long resumeId) {
        return achievementRepository.findByResumeIdOrderBySortOrderAsc(resumeId);
    }

    public Achievement updateAchievement(Long id, Achievement updated) {
        Achievement existing = achievementRepository.findById(id)
                .orElseThrow(() -> new ResumeNotFoundException("Achievement not found"));
        existing.setTitle(updated.getTitle());
        existing.setDescription(updated.getDescription());
        existing.setDate(updated.getDate());
        existing.setSortOrder(updated.getSortOrder());
        return achievementRepository.save(existing);
    }

    public void deleteAchievement(Long id) {
        achievementRepository.deleteById(id);
    }

    public Language addLanguage(Long resumeId, Language language) {
        Resume resume = getResume(resumeId);
        language.setResume(resume);
        return languageRepository.save(language);
    }

    public List<Language> getLanguages(Long resumeId) {
        return languageRepository.findByResumeIdOrderBySortOrderAsc(resumeId);
    }

    public Language updateLanguage(Long id, Language updated) {
        Language existing = languageRepository.findById(id)
                .orElseThrow(() -> new ResumeNotFoundException("Language not found"));
        existing.setName(updated.getName());
        existing.setProficiency(updated.getProficiency());
        existing.setSortOrder(updated.getSortOrder());
        return languageRepository.save(existing);
    }

    public void deleteLanguage(Long id) {
        languageRepository.deleteById(id);
    }
}
