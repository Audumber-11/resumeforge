package com.resumeforge.controller;

import com.resumeforge.entity.Resume;
import com.resumeforge.entity.User;
import com.resumeforge.repository.UserRepository;
import com.resumeforge.security.CustomUserDetails;
import com.resumeforge.service.ResumeService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/dashboard")
public class DashboardController {

    private final ResumeService resumeService;
    private final UserRepository userRepository;

    public DashboardController(ResumeService resumeService, UserRepository userRepository) {
        this.resumeService = resumeService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public String dashboard(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        User user = userDetails.getUser();
        List<Resume> resumes = resumeService.getUserResumes(user.getId());
        long totalResumes = resumeService.countResumes(user.getId());

        model.addAttribute("user", user);
        model.addAttribute("resumes", resumes);
        model.addAttribute("totalResumes", totalResumes);
        return "dashboard";
    }

    @PostMapping("/create")
    public String createResume(@AuthenticationPrincipal CustomUserDetails userDetails,
                               @RequestParam(defaultValue = "Untitled Resume") String title,
                               RedirectAttributes redirectAttributes) {
        User user = userDetails.getUser();
        Resume resume = resumeService.createResume(user.getId(), title, user);
        redirectAttributes.addFlashAttribute("successMessage", "Resume created successfully!");
        return "redirect:/builder/" + resume.getId();
    }

    @PostMapping("/delete/{id}")
    public String deleteResume(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        resumeService.deleteResume(id);
        redirectAttributes.addFlashAttribute("successMessage", "Resume deleted.");
        return "redirect:/dashboard";
    }

    @PostMapping("/duplicate/{id}")
    public String duplicateResume(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        resumeService.duplicateResume(id);
        redirectAttributes.addFlashAttribute("successMessage", "Resume duplicated.");
        return "redirect:/dashboard";
    }
}
