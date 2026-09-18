package com.resumeforge.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * Parses uploaded resume files (PDF, DOCX, TXT) and extracts plain text.
 */
@Service
public class ResumeParserService {

    private static final Logger log = LoggerFactory.getLogger(ResumeParserService.class);

    /**
     * Extract text from an uploaded file.
     * Supports: PDF, DOCX, TXT
     * Returns the extracted text content.
     */
    public String extractText(MultipartFile file) throws IOException {
        String filename = file.getOriginalFilename();
        if (filename == null) filename = "unknown";

        String extension = getFileExtension(filename).toLowerCase();

        return switch (extension) {
            case "pdf" -> extractFromPdf(file);
            case "docx" -> extractFromDocx(file);
            case "txt" -> extractFromTxt(file);
            case "doc" -> extractFromTxt(file); // best effort for .doc
            default -> throw new IllegalArgumentException(
                    "Unsupported file type: " + extension + ". Please upload PDF, DOCX, or TXT.");
        };
    }

    /**
     * Validate a file before parsing.
     * Returns null if valid, or an error message if invalid.
     */
    public String validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return "Please upload a file. The file is empty.";
        }

        String filename = file.getOriginalFilename();
        if (filename == null) {
            return "Invalid file: no filename found.";
        }

        String ext = getFileExtension(filename).toLowerCase();
        if (!ext.matches("pdf|docx|doc|txt")) {
            return "Unsupported file type: " + ext + ". Please upload PDF, DOCX, or TXT.";
        }

        // 5MB max
        if (file.getSize() > 5 * 1024 * 1024) {
            return "File is too large. Maximum size is 5MB.";
        }

        if (file.getSize() == 0) {
            return "File is empty. Please upload a valid resume.";
        }

        return null; // valid
    }

    private String extractFromPdf(MultipartFile file) throws IOException {
        try (PDDocument document = PDDocument.load(file.getBytes())) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            if (text == null || text.isBlank()) {
                throw new IOException("Could not extract text from PDF. The PDF may be image-based.");
            }
            return cleanText(text);
        }
    }

    private String extractFromDocx(MultipartFile file) throws IOException {
        try (XWPFDocument docx = new XWPFDocument(file.getInputStream())) {
            XWPFWordExtractor extractor = new XWPFWordExtractor(docx);
            String text = extractor.getText();
            extractor.close();
            if (text == null || text.isBlank()) {
                throw new IOException("Could not extract text from DOCX. The file may be empty.");
            }
            return cleanText(text);
        }
    }

    private String extractFromTxt(MultipartFile file) throws IOException {
        String text = new String(file.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        if (text.isBlank()) {
            throw new IOException("The text file is empty.");
        }
        return cleanText(text);
    }

    private String cleanText(String text) {
        // Remove excessive whitespace but preserve structure
        return text
                .replaceAll("\\r\\n", "\n")
                .replaceAll("\\r", "\n")
                .replaceAll("\\n{3,}", "\n\n")
                .replaceAll("[ \\t]{2,}", " ")
                .trim();
    }

    private String getFileExtension(String filename) {
        int dot = filename.lastIndexOf('.');
        if (dot >= 0 && dot < filename.length() - 1) {
            return filename.substring(dot + 1);
        }
        return "";
    }
}
