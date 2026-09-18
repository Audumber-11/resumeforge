package com.resumeforge.service;

import com.resumeforge.entity.Resume;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.StringWriter;

@Service
public class PdfService {

    public byte[] generatePdf(Resume resume, String templateHtml) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfRendererBuilder builder = new PdfRendererBuilder()
                    .withHtmlContent(templateHtml, "https://localhost:8080")
                    .toStream(baos);
            registerFont(builder, "Arial", "arial.ttf", "C:/Windows/Fonts");
            registerFont(builder, "Calibri", "calibri.ttf", "C:/Windows/Fonts");
            builder.run();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("PDF generation failed: " + e.getMessage(), e);
        }
    }

    /**
     * Registers a TTF font from the first location that exists, so PDF export works
     * on Windows (C:/Windows/Fonts), Linux servers, and inside Docker containers.
     */
    private void registerFont(PdfRendererBuilder builder, String family, String file, String... searchDirs) {
        for (String dir : searchDirs) {
            java.io.File f = new java.io.File(dir, file);
            if (f.exists()) {
                builder.useFont(f, family);
                return;
            }
        }
        // Last resort: bundled system-font-ish fallbacks on Linux (Liberation Sans is metric-compatible with Arial)
        String[] linuxFallbacks = {
                "/usr/share/fonts/truetype/liberation/LiberationSans-Regular.ttf",
                "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf",
                "/System/Library/Fonts/Supplemental/Arial.ttf"
        };
        for (String path : linuxFallbacks) {
            java.io.File f = new java.io.File(path);
            if (f.exists()) {
                builder.useFont(f, family);
                return;
            }
        }
        // No font file found — openhtmltopdf falls back to its built-in base-14 fonts; PDF still renders.
    }

    public String buildResumeHtml(Resume resume, String template) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html><head><meta charset='UTF-8'/><style>");
        html.append(getTemplateStyles(resume));
        html.append("</style></head><body>");
        html.append(getTemplateBody(resume));
        html.append("</body></html>");
        return html.toString();
    }

    private String getTemplateStyles(Resume resume) {
        String color = resume.getPrimaryColor() != null ? resume.getPrimaryColor() : "#2563eb";
        String font = resume.getFontFamily() != null ? resume.getFontFamily() : "Arial";
        double fontSize = resume.getFontSize() != null ? resume.getFontSize() : 11;
        double margin = resume.getPageMargin() != null ? resume.getPageMargin() : 20;

        return "* { margin: 0; padding: 0; box-sizing: border-box; }" +
                "body { font-family: '" + font + "', sans-serif; font-size: " + fontSize + "pt; color: #1a1a1a; line-height: 1.5; padding: " + margin + "mm; }" +
                ".header { text-align: center; margin-bottom: 16px; border-bottom: 2px solid " + color + "; padding-bottom: 12px; }" +
                ".header h1 { font-size: 22pt; color: " + color + "; margin-bottom: 4px; }" +
                ".header .title { font-size: 12pt; color: #555; margin-bottom: 6px; }" +
                ".header .contact { font-size: 9pt; color: #666; }" +
                ".header .contact span { margin: 0 8px; }" +
                ".section { margin-bottom: 14px; }" +
                ".section-title { font-size: 11pt; font-weight: 700; color: " + color + "; text-transform: uppercase; letter-spacing: 0.5px; border-bottom: 1px solid #ddd; padding-bottom: 4px; margin-bottom: 8px; }" +
                ".summary { font-size: 10pt; color: #333; }" +
                ".entry { margin-bottom: 10px; }" +
                ".entry-header { display: flex; justify-content: space-between; align-items: baseline; }" +
                ".entry-title { font-weight: 600; font-size: 10.5pt; }" +
                ".entry-subtitle { font-size: 10pt; color: #555; }" +
                ".entry-date { font-size: 9pt; color: #777; }" +
                ".entry-desc { font-size: 9.5pt; color: #333; margin-top: 4px; }" +
                ".skills-grid { display: flex; flex-wrap: wrap; gap: 6px; }" +
                ".skill-tag { background: " + color + "15; color: " + color + "; padding: 3px 10px; border-radius: 4px; font-size: 9pt; }" +
                ".social-links { font-size: 9pt; color: #666; }" +
                ".social-links a { color: " + color + "; text-decoration: none; margin-right: 12px; }";
    }

    private String getTemplateBody(Resume resume) {
        StringBuilder body = new StringBuilder();

        // Header
        String fullName = ((resume.getFirstName() != null ? resume.getFirstName() : "") + " " +
                (resume.getLastName() != null ? resume.getLastName() : "")).trim();
        if (fullName.isEmpty()) fullName = "Your Name";

        body.append("<div class='header'>");
        body.append("<h1>").append(escape(fullName)).append("</h1>");
        if (isNotEmpty(resume.getProfessionalTitle())) {
            body.append("<div class='title'>").append(escape(resume.getProfessionalTitle())).append("</div>");
        }

        body.append("<div class='contact'>");
        if (isNotEmpty(resume.getPersonalEmail())) body.append("<span>").append(escape(resume.getPersonalEmail())).append("</span>");
        if (isNotEmpty(resume.getPhone())) body.append("<span>").append(escape(resume.getPhone())).append("</span>");
        if (isNotEmpty(resume.getLocation())) body.append("<span>").append(escape(resume.getLocation())).append("</span>");
        body.append("</div>");

        // Social links in header
        if (isNotEmpty(resume.getLinkedin()) || isNotEmpty(resume.getGithub()) || isNotEmpty(resume.getWebsite())) {
            body.append("<div class='social-links'>");
            if (isNotEmpty(resume.getLinkedin())) body.append("<a href='").append(escape(resume.getLinkedin())).append("'>LinkedIn</a>");
            if (isNotEmpty(resume.getGithub())) body.append("<a href='").append(escape(resume.getGithub())).append("'>GitHub</a>");
            if (isNotEmpty(resume.getWebsite())) body.append("<a href='").append(escape(resume.getWebsite())).append("'>Portfolio</a>");
            body.append("</div>");
        }
        body.append("</div>");

        // Sections in order
        String order = resume.getSectionOrder() != null ? resume.getSectionOrder() : "summary,education,experience,skills,projects,certifications,achievements,languages";
        String[] sections = order.split(",");

        for (String section : sections) {
            switch (section.trim()) {
                case "summary" -> appendSummarySection(body, resume);
                case "education" -> appendEducationSection(body, resume);
                case "experience" -> appendExperienceSection(body, resume);
                case "skills" -> appendSkillsSection(body, resume);
                case "projects" -> appendProjectsSection(body, resume);
                case "certifications" -> appendCertificationsSection(body, resume);
                case "achievements" -> appendAchievementsSection(body, resume);
                case "languages" -> appendLanguagesSection(body, resume);
            }
        }

        return body.toString();
    }

    private void appendSummarySection(StringBuilder body, Resume resume) {
        if (!isNotEmpty(resume.getSummary())) return;
        body.append("<div class='section'><div class='section-title'>Professional Summary</div>");
        body.append("<div class='summary'>").append(escape(resume.getSummary())).append("</div></div>");
    }

    private void appendEducationSection(StringBuilder body, Resume resume) {
        if (resume.getEducationList() == null || resume.getEducationList().isEmpty()) return;
        body.append("<div class='section'><div class='section-title'>Education</div>");
        for (var edu : resume.getEducationList()) {
            body.append("<div class='entry'>");
            body.append("<div class='entry-header'><span class='entry-title'>").append(escape(edu.getInstitution())).append("</span>");
            body.append("<span class='entry-date'>").append(escape(edu.getStartDate())).append(" - ").append(escape(edu.getEndDate())).append("</span></div>");
            if (isNotEmpty(edu.getDegree()) || isNotEmpty(edu.getField())) {
                body.append("<div class='entry-subtitle'>").append(escape(edu.getDegree()));
                if (isNotEmpty(edu.getField())) body.append(" in ").append(escape(edu.getField()));
                body.append("</div>");
            }
            if (isNotEmpty(edu.getGrade())) body.append("<div class='entry-desc'>Grade: ").append(escape(edu.getGrade())).append("</div>");
            if (isNotEmpty(edu.getDescription())) body.append("<div class='entry-desc'>").append(escape(edu.getDescription())).append("</div>");
            body.append("</div>");
        }
        body.append("</div>");
    }

    private void appendExperienceSection(StringBuilder body, Resume resume) {
        if (resume.getExperienceList() == null || resume.getExperienceList().isEmpty()) return;
        body.append("<div class='section'><div class='section-title'>Experience</div>");
        for (var exp : resume.getExperienceList()) {
            body.append("<div class='entry'>");
            body.append("<div class='entry-header'><span class='entry-title'>").append(escape(exp.getJobTitle())).append("</span>");
            body.append("<span class='entry-date'>").append(escape(exp.getStartDate())).append(" - ");
            body.append(Boolean.TRUE.equals(exp.getCurrentlyWorking()) ? "Present" : escape(exp.getEndDate()));
            body.append("</span></div>");
            body.append("<div class='entry-subtitle'>").append(escape(exp.getCompany()));
            if (isNotEmpty(exp.getLocation())) body.append(" · ").append(escape(exp.getLocation()));
            body.append("</div>");
            if (isNotEmpty(exp.getDescription())) body.append("<div class='entry-desc'>").append(escape(exp.getDescription())).append("</div>");
            body.append("</div>");
        }
        body.append("</div>");
    }

    private void appendSkillsSection(StringBuilder body, Resume resume) {
        if (resume.getSkills() == null || resume.getSkills().isEmpty()) return;
        body.append("<div class='section'><div class='section-title'>Skills</div><div class='skills-grid'>");
        for (var skill : resume.getSkills()) {
            body.append("<span class='skill-tag'>").append(escape(skill.getName())).append("</span>");
        }
        body.append("</div></div>");
    }

    private void appendProjectsSection(StringBuilder body, Resume resume) {
        if (resume.getProjects() == null || resume.getProjects().isEmpty()) return;
        body.append("<div class='section'><div class='section-title'>Projects</div>");
        for (var proj : resume.getProjects()) {
            body.append("<div class='entry'>");
            body.append("<div class='entry-header'><span class='entry-title'>").append(escape(proj.getName())).append("</span>");
            if (isNotEmpty(proj.getTechnologies())) {
                body.append("<span class='entry-subtitle'>").append(escape(proj.getTechnologies())).append("</span>");
            }
            body.append("</div>");
            if (isNotEmpty(proj.getDescription())) body.append("<div class='entry-desc'>").append(escape(proj.getDescription())).append("</div>");
            body.append("</div>");
        }
        body.append("</div>");
    }

    private void appendCertificationsSection(StringBuilder body, Resume resume) {
        if (resume.getCertifications() == null || resume.getCertifications().isEmpty()) return;
        body.append("<div class='section'><div class='section-title'>Certifications</div>");
        for (var cert : resume.getCertifications()) {
            body.append("<div class='entry'>");
            body.append("<div class='entry-header'><span class='entry-title'>").append(escape(cert.getName())).append("</span>");
            if (isNotEmpty(cert.getDate())) body.append("<span class='entry-date'>").append(escape(cert.getDate())).append("</span>");
            body.append("</div>");
            if (isNotEmpty(cert.getIssuingOrganization())) body.append("<div class='entry-subtitle'>").append(escape(cert.getIssuingOrganization())).append("</div>");
            body.append("</div>");
        }
        body.append("</div>");
    }

    private void appendAchievementsSection(StringBuilder body, Resume resume) {
        if (resume.getAchievements() == null || resume.getAchievements().isEmpty()) return;
        body.append("<div class='section'><div class='section-title'>Achievements</div>");
        for (var ach : resume.getAchievements()) {
            body.append("<div class='entry'>");
            body.append("<div class='entry-header'><span class='entry-title'>").append(escape(ach.getTitle())).append("</span>");
            if (isNotEmpty(ach.getDate())) body.append("<span class='entry-date'>").append(escape(ach.getDate())).append("</span>");
            body.append("</div>");
            if (isNotEmpty(ach.getDescription())) body.append("<div class='entry-desc'>").append(escape(ach.getDescription())).append("</div>");
            body.append("</div>");
        }
        body.append("</div>");
    }

    private void appendLanguagesSection(StringBuilder body, Resume resume) {
        if (resume.getLanguages() == null || resume.getLanguages().isEmpty()) return;
        body.append("<div class='section'><div class='section-title'>Languages</div><div class='skills-grid'>");
        for (var lang : resume.getLanguages()) {
            body.append("<span class='skill-tag'>").append(escape(lang.getName()));
            if (isNotEmpty(lang.getProficiency())) body.append(" (").append(escape(lang.getProficiency())).append(")");
            body.append("</span>");
        }
        body.append("</div></div>");
    }

    private boolean isNotEmpty(String s) {
        return s != null && !s.trim().isEmpty();
    }

    private String escape(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }
}
