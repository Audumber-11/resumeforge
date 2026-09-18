package com.resumeforge.controller;

import com.resumeforge.entity.Resume;
import com.resumeforge.security.CustomUserDetails;
import com.resumeforge.service.PdfService;
import com.resumeforge.service.ResumeService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class PdfController {

    private final ResumeService resumeService;
    private final PdfService pdfService;

    public PdfController(ResumeService resumeService, PdfService pdfService) {
        this.resumeService = resumeService;
        this.pdfService = pdfService;
    }

    @GetMapping("/resume/{id}/pdf")
    public void downloadPdf(@PathVariable Long id,
                            @AuthenticationPrincipal CustomUserDetails userDetails,
                            HttpServletResponse response) {
        Resume resume = resumeService.getResume(id);

        if (!resume.getUser().getId().equals(userDetails.getUser().getId())) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        String html = pdfService.buildResumeHtml(resume, resume.getTemplateId());
        byte[] pdf = pdfService.generatePdf(resume, html);

        String filename = "Resume_" + (resume.getFirstName() != null ? resume.getFirstName() : "Export") + ".pdf";
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");

        try {
            response.getOutputStream().write(pdf);
            response.getOutputStream().flush();
        } catch (Exception e) {
            throw new RuntimeException("Failed to write PDF", e);
        }
    }
}
