package com.githealth.githealth.analytics;

import com.githealth.githealth.dto.RepositoryResponse;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class RoleAnalysisService {

    /*
     * =====================================================
     * ROLE SKILL DEFINITIONS
     * =====================================================
     */

    private final Map<String, List<String>> roleSkills =
            new LinkedHashMap<>();

    public RoleAnalysisService() {

        roleSkills.put(
                "Java Backend Developer",
                Arrays.asList(
                        "Java",
                        "Spring",
                        "Spring Boot",
                        "SQL",
                        "Git"
                )
        );

        roleSkills.put(
                "Full Stack Developer",
                Arrays.asList(
                        "Java",
                        "JavaScript",
                        "HTML",
                        "CSS",
                        "SQL",
                        "Git"
                )
        );

        roleSkills.put(
                "Python Developer",
                Arrays.asList(
                        "Python",
                        "SQL",
                        "Git",
                        "REST API"
                )
        );

        roleSkills.put(
                "Frontend Developer",
                Arrays.asList(
                        "JavaScript",
                        "TypeScript",
                        "HTML",
                        "CSS"
                )
        );

        roleSkills.put(
                "Data Analyst",
                Arrays.asList(
                        "Python",
                        "R",
                        "SQL",
                        "Excel"
                )
        );
    }


    /*
     * =====================================================
     * ANALYZE TARGET ROLE
     * =====================================================
     */

    public Map<String, Object> analyzeRole(
            String role,
            List<RepositoryResponse> repositories) {

        Map<String, Object> result =
                new LinkedHashMap<>();


        /*
         * Check whether role exists
         */

        if (!roleSkills.containsKey(role)) {

            result.put(
                    "error",
                    "Unsupported target role"
            );

            result.put(
                    "availableRoles",
                    new ArrayList<>(
                            roleSkills.keySet()
                    )
            );

            return result;
        }


        /*
         * =================================================
         * DETECT GITHUB SKILLS
         * =================================================
         */

        Set<String> detectedSkills =
                new LinkedHashSet<>();


        for (RepositoryResponse repo : repositories) {

            String language =
                    repo.getLanguage();

            if (
                    language != null
                            &&
                    !language.isBlank()
            ) {

                detectedSkills.add(
                        language
                );
            }
        }


        /*
         * =================================================
         * TARGET ROLE SKILLS
         * =================================================
         */

        List<String> requiredSkills =
                roleSkills.get(role);


        /*
         * =================================================
         * MATCHED SKILLS
         * =================================================
         */

        List<String> matchedSkills =
                new ArrayList<>();

        List<String> skillsToDevelop =
                new ArrayList<>();


        for (String skill : requiredSkills) {

            if (
                    containsSkill(
                            detectedSkills,
                            skill
                    )
            ) {

                matchedSkills.add(
                        skill
                );

            } else {

                skillsToDevelop.add(
                        skill
                );
            }
        }


        /*
         * =================================================
         * ROLE COVERAGE
         * =================================================
         */

        int totalRequiredSkills =
                requiredSkills.size();

        int matchedSkillCount =
                matchedSkills.size();

        double coverage =
                0;

        if (totalRequiredSkills > 0) {

            coverage =
                    (
                            matchedSkillCount
                                    * 100.0
                    )
                            /
                    totalRequiredSkills;

            coverage =
                    Math.round(
                            coverage * 10.0
                    )
                            /
                    10.0;
        }


        /*
         * =================================================
         * RESULT
         * =================================================
         */

        result.put(
                "targetRole",
                role
        );

        result.put(
                "requiredSkills",
                requiredSkills
        );

        result.put(
                "detectedSkills",
                new ArrayList<>(
                        detectedSkills
                )
        );

        result.put(
                "matchedSkills",
                matchedSkills
        );

        result.put(
                "skillsToDevelop",
                skillsToDevelop
        );

        result.put(
                "totalRequiredSkills",
                totalRequiredSkills
        );

        result.put(
                "matchedSkillCount",
                matchedSkillCount
        );

        result.put(
                "skillsToDevelopCount",
                skillsToDevelop.size()
        );

        result.put(
                "coveragePercentage",
                coverage
        );

        return result;
    }


    /*
     * =====================================================
     * SKILL MATCHING
     * =====================================================
     */

    private boolean containsSkill(
            Set<String> detectedSkills,
            String requiredSkill) {

        for (String detectedSkill :
                detectedSkills) {

            if (
                    detectedSkill.equalsIgnoreCase(
                            requiredSkill
                    )
            ) {

                return true;
            }
        }

        return false;
    }


    /*
     * =====================================================
     * AVAILABLE ROLES
     * =====================================================
     */

    public List<String> getAvailableRoles() {

        return new ArrayList<>(
                roleSkills.keySet()
        );
    }
}