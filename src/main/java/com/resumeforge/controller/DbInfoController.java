package com.resumeforge.controller;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class DbInfoController {

    private final JdbcTemplate jdbc;

    public DbInfoController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @GetMapping("/db-info/tables")
    public List<Map<String, Object>> getTables() {
        return jdbc.queryForList(
            "SELECT TABLE_NAME, TABLE_TYPE FROM INFORMATION_SCHEMA.TABLES " +
            "WHERE TABLE_SCHEMA = 'PUBLIC' ORDER BY TABLE_NAME"
        );
    }

    @GetMapping("/db-info/data")
    public Map<String, Object> getData() {
        java.util.LinkedHashMap<String, Object> result = new java.util.LinkedHashMap<>();
        String[] tables = {"USERS", "RESUMES", "EDUCATION", "EXPERIENCE", "SKILLS",
                           "PROJECTS", "CERTIFICATIONS", "ACHIEVEMENTS", "LANGUAGES",
                           "SOCIAL_LINKS", "AI_ANALYSES", "AI_CONVERSATIONS", "AI_MESSAGES"};
        for (String table : tables) {
            try {
                List<Map<String, Object>> rows = jdbc.queryForList("SELECT * FROM " + table);
                result.put(table, rows);
            } catch (Exception e) {
                result.put(table, "Error: " + e.getMessage());
            }
        }
        return result;
    }
}
