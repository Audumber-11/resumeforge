package com.resumeforge.controller;

import com.resumeforge.entity.*;
import com.resumeforge.security.CustomUserDetails;
import com.resumeforge.service.ResumeService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/builder")
public class ResumeController {

    private final ResumeService resumeService;

    public ResumeController(ResumeService resumeService) {
        this.resumeService = resumeService;
    }

    // ── Builder Page ────────────────────────────────────────

    @GetMapping("/{id}")
    public String builder(@PathVariable Long id,
                          @AuthenticationPrincipal CustomUserDetails userDetails,
                          Model model) {
        Resume resume = resumeService.getResume(id);

        // Security check
        if (!resume.getUser().getId().equals(userDetails.getUser().getId())) {
            return "redirect:/dashboard";
        }

        List<Education> educationList = resumeService.getEducationList(id);
        List<Experience> experienceList = resumeService.getExperienceList(id);
        List<Skill> skills = resumeService.getSkills(id);
        List<Project> projects = resumeService.getProjects(id);
        List<Certification> certifications = resumeService.getCertifications(id);
        List<Achievement> achievements = resumeService.getAchievements(id);
        List<Language> languages = resumeService.getLanguages(id);
        List<SocialLink> socialLinks = resumeService.getSocialLinks(id);

        model.addAttribute("resume", resume);
        model.addAttribute("educationList", educationList);
        model.addAttribute("experienceList", experienceList);
        model.addAttribute("skills", skills);
        model.addAttribute("projects", projects);
        model.addAttribute("certifications", certifications);
        model.addAttribute("achievements", achievements);
        model.addAttribute("languages", languages);
        model.addAttribute("socialLinks", socialLinks);
        model.addAttribute("user", userDetails.getUser());

        return "builder";
    }

    // ── Save Resume Personal Info ───────────────────────────

    @PostMapping("/{id}/save")
    @ResponseBody
    public Map<String, Object> saveResume(@PathVariable Long id, @RequestBody Resume updated) {
        Map<String, Object> result = new HashMap<>();
        try {
            Resume saved = resumeService.updateResume(id, updated);
            result.put("success", true);
            result.put("resume", saved);
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }

    // ── Education ───────────────────────────────────────────

    @PostMapping("/{id}/education")
    @ResponseBody
    public Map<String, Object> addEducation(@PathVariable Long id, @RequestBody Education education) {
        Map<String, Object> result = new HashMap<>();
        try {
            Education saved = resumeService.addEducation(id, education);
            result.put("success", true);
            result.put("item", saved);
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }

    @PutMapping("/education/{id}")
    @ResponseBody
    public Map<String, Object> updateEducation(@PathVariable Long id, @RequestBody Education education) {
        Map<String, Object> result = new HashMap<>();
        try {
            Education saved = resumeService.updateEducation(id, education);
            result.put("success", true);
            result.put("item", saved);
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }

    @DeleteMapping("/education/{id}")
    @ResponseBody
    public Map<String, Object> deleteEducation(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        try {
            resumeService.deleteEducation(id);
            result.put("success", true);
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }

    // ── Experience ──────────────────────────────────────────

    @PostMapping("/{id}/experience")
    @ResponseBody
    public Map<String, Object> addExperience(@PathVariable Long id, @RequestBody Experience experience) {
        Map<String, Object> result = new HashMap<>();
        try {
            Experience saved = resumeService.addExperience(id, experience);
            result.put("success", true);
            result.put("item", saved);
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }

    @PutMapping("/experience/{id}")
    @ResponseBody
    public Map<String, Object> updateExperience(@PathVariable Long id, @RequestBody Experience experience) {
        Map<String, Object> result = new HashMap<>();
        try {
            Experience saved = resumeService.updateExperience(id, experience);
            result.put("success", true);
            result.put("item", saved);
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }

    @DeleteMapping("/experience/{id}")
    @ResponseBody
    public Map<String, Object> deleteExperience(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        try {
            resumeService.deleteExperience(id);
            result.put("success", true);
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }

    // ── Skills ──────────────────────────────────────────────

    @PostMapping("/{id}/skills")
    @ResponseBody
    public Map<String, Object> addSkill(@PathVariable Long id, @RequestBody Skill skill) {
        Map<String, Object> result = new HashMap<>();
        try {
            Skill saved = resumeService.addSkill(id, skill);
            result.put("success", true);
            result.put("item", saved);
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }

    @PutMapping("/skills/{id}")
    @ResponseBody
    public Map<String, Object> updateSkill(@PathVariable Long id, @RequestBody Skill skill) {
        Map<String, Object> result = new HashMap<>();
        try {
            Skill saved = resumeService.updateSkill(id, skill);
            result.put("success", true);
            result.put("item", saved);
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }

    @DeleteMapping("/skills/{id}")
    @ResponseBody
    public Map<String, Object> deleteSkill(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        try {
            resumeService.deleteSkill(id);
            result.put("success", true);
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }

    // ── Projects ────────────────────────────────────────────

    @PostMapping("/{id}/projects")
    @ResponseBody
    public Map<String, Object> addProject(@PathVariable Long id, @RequestBody Project project) {
        Map<String, Object> result = new HashMap<>();
        try {
            Project saved = resumeService.addProject(id, project);
            result.put("success", true);
            result.put("item", saved);
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }

    @PutMapping("/projects/{id}")
    @ResponseBody
    public Map<String, Object> updateProject(@PathVariable Long id, @RequestBody Project project) {
        Map<String, Object> result = new HashMap<>();
        try {
            Project saved = resumeService.updateProject(id, project);
            result.put("success", true);
            result.put("item", saved);
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }

    @DeleteMapping("/projects/{id}")
    @ResponseBody
    public Map<String, Object> deleteProject(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        try {
            resumeService.deleteProject(id);
            result.put("success", true);
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }

    // ── Certifications ─────────────────────────────────────

    @PostMapping("/{id}/certifications")
    @ResponseBody
    public Map<String, Object> addCertification(@PathVariable Long id, @RequestBody Certification cert) {
        Map<String, Object> result = new HashMap<>();
        try {
            Certification saved = resumeService.addCertification(id, cert);
            result.put("success", true);
            result.put("item", saved);
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }

    @PutMapping("/certifications/{id}")
    @ResponseBody
    public Map<String, Object> updateCertification(@PathVariable Long id, @RequestBody Certification cert) {
        Map<String, Object> result = new HashMap<>();
        try {
            Certification saved = resumeService.updateCertification(id, cert);
            result.put("success", true);
            result.put("item", saved);
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }

    @DeleteMapping("/certifications/{id}")
    @ResponseBody
    public Map<String, Object> deleteCertification(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        try {
            resumeService.deleteCertification(id);
            result.put("success", true);
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }

    // ── Achievements ────────────────────────────────────────

    @PostMapping("/{id}/achievements")
    @ResponseBody
    public Map<String, Object> addAchievement(@PathVariable Long id, @RequestBody Achievement achievement) {
        Map<String, Object> result = new HashMap<>();
        try {
            Achievement saved = resumeService.addAchievement(id, achievement);
            result.put("success", true);
            result.put("item", saved);
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }

    @PutMapping("/achievements/{id}")
    @ResponseBody
    public Map<String, Object> updateAchievement(@PathVariable Long id, @RequestBody Achievement achievement) {
        Map<String, Object> result = new HashMap<>();
        try {
            Achievement saved = resumeService.updateAchievement(id, achievement);
            result.put("success", true);
            result.put("item", saved);
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }

    @DeleteMapping("/achievements/{id}")
    @ResponseBody
    public Map<String, Object> deleteAchievement(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        try {
            resumeService.deleteAchievement(id);
            result.put("success", true);
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }

    // ── Languages ──────────────────────────────────────────

    @PostMapping("/{id}/languages")
    @ResponseBody
    public Map<String, Object> addLanguage(@PathVariable Long id, @RequestBody Language language) {
        Map<String, Object> result = new HashMap<>();
        try {
            Language saved = resumeService.addLanguage(id, language);
            result.put("success", true);
            result.put("item", saved);
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }

    @PutMapping("/languages/{id}")
    @ResponseBody
    public Map<String, Object> updateLanguage(@PathVariable Long id, @RequestBody Language language) {
        Map<String, Object> result = new HashMap<>();
        try {
            Language saved = resumeService.updateLanguage(id, language);
            result.put("success", true);
            result.put("item", saved);
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }

    @DeleteMapping("/languages/{id}")
    @ResponseBody
    public Map<String, Object> deleteLanguage(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        try {
            resumeService.deleteLanguage(id);
            result.put("success", true);
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }

    // ── Social Links ──────────────────────────────────────

    @PostMapping("/{id}/sociallinks")
    @ResponseBody
    public Map<String, Object> addSocialLink(@PathVariable Long id, @RequestBody SocialLink link) {
        Map<String, Object> result = new HashMap<>();
        try {
            SocialLink saved = resumeService.addSocialLink(id, link);
            result.put("success", true);
            result.put("item", saved);
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }

    @PutMapping("/sociallinks/{id}")
    @ResponseBody
    public Map<String, Object> updateSocialLink(@PathVariable Long id, @RequestBody SocialLink link) {
        Map<String, Object> result = new HashMap<>();
        try {
            SocialLink saved = resumeService.updateSocialLink(id, link);
            result.put("success", true);
            result.put("item", saved);
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }

    @DeleteMapping("/sociallinks/{id}")
    @ResponseBody
    public Map<String, Object> deleteSocialLink(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        try {
            resumeService.deleteSocialLink(id);
            result.put("success", true);
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }
}
