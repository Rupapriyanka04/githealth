package com.githealth.githealth.analytics;

import com.githealth.githealth.dto.RepositoryResponse;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class SkillRoadmapService {

    public Map<String, Object> generateRoadmap(
            String role,
            List<RepositoryResponse> repositories) {

        Map<String, Object> result = new LinkedHashMap<>();
        Map<String, List<String>> roleSkills = getRoleSkills();

        if (!roleSkills.containsKey(role)) {
            result.put("error", "Unsupported target role");
            result.put("availableRoles", new ArrayList<>(roleSkills.keySet()));
            return result;
        }

        Set<String> detectedSkills = new LinkedHashSet<>();

        for (RepositoryResponse repo : repositories) {
            String language = repo.getLanguage();

            if (language != null && !language.isBlank()) {
                detectedSkills.add(language);
            }
        }

        List<Map<String, Object>> roadmap = new ArrayList<>();
        List<String> requiredSkills = roleSkills.get(role);

        for (int i = 0; i < requiredSkills.size(); i++) {
            String skill = requiredSkills.get(i);
            boolean detected = containsSkill(detectedSkills, skill);

            Map<String, Object> step = new LinkedHashMap<>();
            step.put("step", i + 1);
            step.put("skill", skill);
            step.put("status", detected ? "Detected" : "Develop");
            step.put("completed", detected);

            roadmap.add(step);
        }

        result.put("targetRole", role);
        result.put("roadmap", roadmap);
        result.put("totalSteps", roadmap.size());

        return result;
    }

    private Map<String, List<String>> getRoleSkills() {
        Map<String, List<String>> roles = new LinkedHashMap<>();

        roles.put(
                "Java Backend Developer",
                List.of(
                        "Java",
                        "Spring",
                        "Spring Boot",
                        "SQL",
                        "Git"
                )
        );

        roles.put(
                "Full Stack Developer",
                List.of(
                        "Java",
                        "JavaScript",
                        "HTML",
                        "CSS",
                        "SQL",
                        "Git"
                )
        );

        roles.put(
                "Python Developer",
                List.of(
                        "Python",
                        "SQL",
                        "Git",
                        "REST API"
                )
        );

        roles.put(
                "Frontend Developer",
                List.of(
                        "JavaScript",
                        "TypeScript",
                        "HTML",
                        "CSS"
                )
        );

        roles.put(
                "Data Analyst",
                List.of(
                        "Python",
                        "R",
                        "SQL",
                        "Excel"
                )
        );

        return roles;
    }

    private boolean containsSkill(
            Set<String> detectedSkills,
            String requiredSkill) {

        for (String skill : detectedSkills) {
            if (skill.equalsIgnoreCase(requiredSkill)) {
                return true;
            }
        }

        return false;
    }
}