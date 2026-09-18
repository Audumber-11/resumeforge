package com.resumeforge;

import com.resumeforge.entity.*;
import com.resumeforge.exception.ResumeNotFoundException;
import com.resumeforge.repository.UserRepository;
import com.resumeforge.service.AuthService;
import com.resumeforge.service.ResumeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ResumeServiceTest {

    @Autowired
    private ResumeService resumeService;

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setup() {
        userRepository.deleteAll();
        testUser = authService.register("Test User", "testuser", "test@example.com", "password123");
    }

    @Test
    void testCreateResume() {
        Resume resume = resumeService.createResume(testUser.getId(), "My Resume", testUser);
        assertNotNull(resume);
        assertEquals("My Resume", resume.getTitle());
        assertEquals(testUser.getId(), resume.getUser().getId());
    }

    @Test
    void testGetResume() {
        Resume created = resumeService.createResume(testUser.getId(), "My Resume", testUser);
        Resume fetched = resumeService.getResume(created.getId());
        assertNotNull(fetched);
        assertEquals("My Resume", fetched.getTitle());
    }

    @Test
    void testGetResumeNotFound() {
        assertThrows(ResumeNotFoundException.class, () ->
            resumeService.getResume(999L)
        );
    }

    @Test
    void testUpdateResume() {
        Resume created = resumeService.createResume(testUser.getId(), "My Resume", testUser);
        created.setTitle("Updated Resume");
        created.setFirstName("John");
        created.setLastName("Doe");
        resumeService.updateResume(created.getId(), created);

        Resume updated = resumeService.getResume(created.getId());
        assertEquals("Updated Resume", updated.getTitle());
        assertEquals("John", updated.getFirstName());
        assertEquals("Doe", updated.getLastName());
    }

    @Test
    void testDeleteResume() {
        Resume created = resumeService.createResume(testUser.getId(), "My Resume", testUser);
        resumeService.deleteResume(created.getId());
        assertThrows(ResumeNotFoundException.class, () ->
            resumeService.getResume(created.getId())
        );
    }

    @Test
    void testDuplicateResume() {
        Resume original = resumeService.createResume(testUser.getId(), "Original", testUser);
        original.setFirstName("John");
        resumeService.updateResume(original.getId(), original);

        Resume copy = resumeService.duplicateResume(original.getId());
        assertNotNull(copy);
        assertTrue(copy.getTitle().contains("Copy"));
        assertEquals("John", copy.getFirstName());
    }

    @Test
    void testAddEducation() {
        Resume resume = resumeService.createResume(testUser.getId(), "My Resume", testUser);
        Education edu = new Education(resume);
        edu.setInstitution("MIT");
        edu.setDegree("BCA");
        edu.setField("Computer Science");
        edu.setStartDate("2024");
        edu.setEndDate("2027");

        Education saved = resumeService.addEducation(resume.getId(), edu);
        assertNotNull(saved);
        assertEquals("MIT", saved.getInstitution());

        List<Education> eduList = resumeService.getEducationList(resume.getId());
        assertEquals(1, eduList.size());
    }

    @Test
    void testAddExperience() {
        Resume resume = resumeService.createResume(testUser.getId(), "My Resume", testUser);
        Experience exp = new Experience(resume);
        exp.setJobTitle("Java Developer");
        exp.setCompany("ABC Tech");
        exp.setStartDate("2024-06");
        exp.setEndDate("2024-08");

        Experience saved = resumeService.addExperience(resume.getId(), exp);
        assertNotNull(saved);
        assertEquals("Java Developer", saved.getJobTitle());

        List<Experience> expList = resumeService.getExperienceList(resume.getId());
        assertEquals(1, expList.size());
    }

    @Test
    void testAddSkill() {
        Resume resume = resumeService.createResume(testUser.getId(), "My Resume", testUser);
        Skill skill = new Skill(resume);
        skill.setName("Java");
        skill.setCategory("Programming");
        skill.setProficiency("Advanced");

        Skill saved = resumeService.addSkill(resume.getId(), skill);
        assertNotNull(saved);
        assertEquals("Java", saved.getName());

        List<Skill> skills = resumeService.getSkills(resume.getId());
        assertEquals(1, skills.size());
    }

    @Test
    void testAddProject() {
        Resume resume = resumeService.createResume(testUser.getId(), "My Resume", testUser);
        Project project = new Project(resume);
        project.setName("ResumeCraft AI");
        project.setDescription("AI-powered resume builder");
        project.setTechnologies("Java, Spring Boot, MySQL");

        Project saved = resumeService.addProject(resume.getId(), project);
        assertNotNull(saved);
        assertEquals("ResumeCraft AI", saved.getName());
    }

    @Test
    void testGetUserResumes() {
        resumeService.createResume(testUser.getId(), "Resume 1", testUser);
        resumeService.createResume(testUser.getId(), "Resume 2", testUser);

        List<Resume> resumes = resumeService.getUserResumes(testUser.getId());
        assertEquals(2, resumes.size());
    }
}
