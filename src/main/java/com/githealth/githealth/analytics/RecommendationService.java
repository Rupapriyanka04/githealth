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
public class RecommendationService {

    public Map<String, Object> generateRecommendations(
            String role,
            List<RepositoryResponse> repositories) {

        Map<String, Object> result = new LinkedHashMap<>();

        Set<String> detectedSkills = new LinkedHashSet<>();

        for (RepositoryResponse repo : repositories) {
            String language = repo.getLanguage();

            if (language != null && !language.isBlank()) {
                detectedSkills.add(language);
            }
        }

        Map<String, List<String>> roleSkills = getRoleSkills();

        if (!roleSkills.containsKey(role)) {
            result.put("error", "Unsupported target role");
            result.put("availableRoles", new ArrayList<>(roleSkills.keySet()));
            return result;
        }

        List<String> requiredSkills = roleSkills.get(role);
        List<String> detected = new ArrayList<>(detectedSkills);
        List<String> recommendations = new ArrayList<>();

        for (String skill : requiredSkills) {
            if (!containsSkill(detectedSkills, skill)) {
                recommendations.add(skill);
            }
        }

        List<String> learningPlan = createLearningPlan(recommendations);

        result.put("targetRole", role);
        result.put("detectedSkills", detected);
        result.put("recommendedSkills", recommendations);
        result.put("recommendationCount", recommendations.size());
        result.put("learningPlan", learningPlan);

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

    private List<String> createLearningPlan(
            List<String> recommendations) {

        List<String> plan = new ArrayList<>();

        for (String skill : recommendations) {
            switch (skill.toLowerCase()) {
                case "java":
                    plan.add("Strengthen Java fundamentals and object-oriented programming.");
                    break;
                case "spring":
                    plan.add("Learn Spring fundamentals, dependency injection and REST development.");
                    break;
                case "spring boot":
                    plan.add("Build REST APIs using Spring Boot.");
                    break;
                case "sql":
                    plan.add("Practice SQL queries, joins, aggregation and database design.");
                    break;
                case "git":
                    plan.add("Practice Git branching, merging and collaborative workflows.");
                    break;
                case "javascript":
                    plan.add("Practice modern JavaScript and browser-based application development.");
                    break;
                case "html":
                    plan.add("Build semantic and responsive web pages using HTML.");
                    break;
                case "css":
                    plan.add("Practice responsive layouts, Flexbox and Grid.");
                    break;
                case "typescript":
                    plan.add("Learn TypeScript types, interfaces and modern application patterns.");
                    break;
                case "python":
                    plan.add("Strengthen Python programming and problem-solving skills.");
                    break;
                case "rest api":
                    plan.add("Build and consume REST APIs using HTTP and JSON.");
                    break;
                case "r":
                    plan.add("Practice R programming and data analysis workflows.");
                    break;
                case "excel":
                    plan.add("Practice Excel formulas, data cleaning and analysis.");
                    break;
                default:
                    plan.add("Build a practical project using " + skill + ".");
            }
        }

        return plan;
    }

    private boolean containsSkill(
            Set<String> detectedSkills,
            String requiredSkill) {

        for (String detectedSkill : detectedSkills) {
            if (detectedSkill.equalsIgnoreCase(requiredSkill)) {
                return true;
            }
        }

        return false;
    }
}