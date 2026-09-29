package com.githealth.githealth.analytics;

import com.githealth.githealth.dto.RepositoryResponse;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class DeveloperInsights {

    public Map<String, Object> analyze(List<RepositoryResponse> repositories) {
        Map<String, Object> insights = new LinkedHashMap<>();

        int totalRepositories = repositories.size();
        int totalStars = 0;
        int totalForks = 0;
        Map<String, Integer> languageCounts = new HashMap<>();

        for (RepositoryResponse repo : repositories) {
            totalStars += repo.getStars();
            totalForks += repo.getForks();

            String language = repo.getLanguage();

            if (language != null && !language.isBlank()) {
                languageCounts.put(
                        language,
                        languageCounts.getOrDefault(language, 0) + 1
                );
            }
        }

        String primaryLanguage = "Not detected";

        if (!languageCounts.isEmpty()) {
            primaryLanguage = languageCounts.entrySet()
                    .stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse("Not detected");
        }

        String mostPopularRepository = "None";
        int mostPopularRepositoryStars = 0;

        if (!repositories.isEmpty()) {
            RepositoryResponse popularRepo = repositories.stream()
                    .max(Comparator.comparingInt(RepositoryResponse::getStars))
                    .orElse(null);

            if (popularRepo != null) {
                mostPopularRepository = popularRepo.getName();
                mostPopularRepositoryStars = popularRepo.getStars();
            }
        }

        List<Map<String, Object>> topRepositories = new ArrayList<>();

        repositories.stream()
                .sorted(
                        Comparator.comparingInt(
                                RepositoryResponse::getStars
                        ).reversed()
                )
                .limit(3)
                .forEach(repo -> {
                    Map<String, Object> item = new LinkedHashMap<>();

                    item.put("name", repo.getName());
                    item.put("stars", repo.getStars());
                    item.put("forks", repo.getForks());

                    topRepositories.add(item);
                });

        Map<String, Double> languagePercentages =
                new LinkedHashMap<>();

        if (totalRepositories > 0) {
            languageCounts.entrySet()
                    .stream()
                    .sorted(
                            Map.Entry
                                    .<String, Integer>comparingByValue()
                                    .reversed()
                    )
                    .forEach(entry -> {
                        double percentage =
                                entry.getValue() * 100.0
                                        / totalRepositories;

                        percentage =
                                Math.round(percentage * 10.0) / 10.0;

                        languagePercentages.put(
                                entry.getKey(),
                                percentage
                        );
                    });
        }

        int repositoryScore =
                Math.min(totalRepositories * 5, 25);

        int starScore =
                Math.min(totalStars / 100, 25);

        int forkScore =
                Math.min(totalForks / 100, 20);

        int languageScore =
                Math.min(languageCounts.size() * 5, 15);

        int popularityScore =
                Math.min(mostPopularRepositoryStars / 1000, 15);

        int gitHealthScore =
                repositoryScore
                        + starScore
                        + forkScore
                        + languageScore
                        + popularityScore;

        List<RepositoryResponse> recentRepositories =
                new ArrayList<>();

        Instant activityLimit =
                Instant.now().minus(
                        180,
                        ChronoUnit.DAYS
                );

        for (RepositoryResponse repo : repositories) {
            String pushedAt = repo.getPushedAt();

            if (pushedAt == null || pushedAt.isBlank()) {
                continue;
            }

            try {
                Instant pushedDate =
                        Instant.parse(pushedAt);

                if (pushedDate.isAfter(activityLimit)) {
                    recentRepositories.add(repo);
                }
            } catch (Exception ignored) {
                // Ignore invalid GitHub dates
            }
        }

        recentRepositories.sort(
                Comparator.comparing(
                        RepositoryResponse::getPushedAt,
                        Comparator.nullsLast(
                                Comparator.reverseOrder()
                        )
                )
        );

        List<Map<String, Object>> recentRepositoryData =
                new ArrayList<>();

        recentRepositories
                .stream()
                .limit(5)
                .forEach(repo -> {
                    Map<String, Object> item =
                            new LinkedHashMap<>();

                    item.put(
                            "name",
                            repo.getName()
                    );

                    item.put(
                            "pushedAt",
                            repo.getPushedAt()
                    );

                    recentRepositoryData.add(item);
                });

        int recentActivityCount =
                recentRepositories.size();

        String activityLevel;

        if (recentActivityCount >= 10) {
            activityLevel = "Very Active";
        } else if (recentActivityCount >= 5) {
            activityLevel = "Active";
        } else if (recentActivityCount >= 2) {
            activityLevel = "Moderately Active";
        } else if (recentActivityCount == 1) {
            activityLevel = "Low Activity";
        } else {
            activityLevel = "Inactive";
        }

        int totalQualityScore = 0;
        int excellentRepositories = 0;
        int goodRepositories = 0;
        int moderateRepositories = 0;
        int basicRepositories = 0;

        for (RepositoryResponse repo : repositories) {
            int qualityScore =
                    repo.getQualityScore();

            totalQualityScore += qualityScore;

            if (qualityScore >= 80) {
                excellentRepositories++;
            } else if (qualityScore >= 60) {
                goodRepositories++;
            } else if (qualityScore >= 40) {
                moderateRepositories++;
            } else {
                basicRepositories++;
            }
        }

        double averageQualityScore = 0;

        if (totalRepositories > 0) {
            averageQualityScore =
                    (double) totalQualityScore
                            / totalRepositories;

            averageQualityScore =
                    Math.round(
                            averageQualityScore * 10.0
                    ) / 10.0;
        }

        String overallQualityLevel;

        if (averageQualityScore >= 80) {
            overallQualityLevel = "Excellent";
        } else if (averageQualityScore >= 60) {
            overallQualityLevel = "Good";
        } else if (averageQualityScore >= 40) {
            overallQualityLevel = "Moderate";
        } else {
            overallQualityLevel = "Basic";
        }

        /*
         * =====================================================
         * ADVANCED SKILL EVIDENCE DETECTION
         * =====================================================
         */

        Map<String, List<String>> skillKeywords =
                new LinkedHashMap<>();

        skillKeywords.put(
                "Java",
                List.of("java")
        );

        skillKeywords.put(
                "Spring",
                List.of("spring")
        );

        skillKeywords.put(
                "Spring Boot",
                List.of("spring boot", "springboot")
        );

        skillKeywords.put(
                "Python",
                List.of("python")
        );

        skillKeywords.put(
                "JavaScript",
                List.of("javascript", "js")
        );

        skillKeywords.put(
                "TypeScript",
                List.of("typescript", "ts")
        );

        skillKeywords.put(
                "HTML",
                List.of("html")
        );

        skillKeywords.put(
                "CSS",
                List.of("css")
        );

        skillKeywords.put(
                "React",
                List.of("react", "reactjs")
        );

        skillKeywords.put(
                "Angular",
                List.of("angular")
        );

        skillKeywords.put(
                "Vue.js",
                List.of("vue", "vuejs")
        );

        skillKeywords.put(
                "Node.js",
                List.of("node", "node.js", "nodejs")
        );

        skillKeywords.put(
                "SQL",
                List.of(
                        "sql",
                        "mysql",
                        "postgresql",
                        "postgres"
                )
        );

        skillKeywords.put(
                "MongoDB",
                List.of(
                        "mongodb",
                        "mongo"
                )
        );

        skillKeywords.put(
                "REST API",
                List.of(
                        "rest api",
                        "rest-api",
                        "restful"
                )
        );

        skillKeywords.put(
                "Machine Learning",
                List.of(
                        "machine learning",
                        "ml"
                )
        );

        skillKeywords.put(
                "Deep Learning",
                List.of(
                        "deep learning"
                )
        );

        skillKeywords.put(
                "Data Science",
                List.of(
                        "data science",
                        "datascience"
                )
        );

        skillKeywords.put(
                "TensorFlow",
                List.of(
                        "tensorflow"
                )
        );

        skillKeywords.put(
                "PyTorch",
                List.of(
                        "pytorch"
                )
        );

        skillKeywords.put(
                "Docker",
                List.of(
                        "docker"
                )
        );

        skillKeywords.put(
                "Kubernetes",
                List.of(
                        "kubernetes",
                        "k8s"
                )
        );

        skillKeywords.put(
                "Git",
                List.of(
                        "git",
                        "github"
                )
        );

        skillKeywords.put(
                "C",
                List.of(
                        "c programming"
                )
        );

        skillKeywords.put(
                "C++",
                List.of(
                        "c++",
                        "cpp"
                )
        );

        skillKeywords.put(
                "C#",
                List.of(
                        "c#",
                        "csharp"
                )
        );

        List<Map<String, Object>> skillEvidence =
                new ArrayList<>();

        for (
                Map.Entry<String, List<String>> skillEntry
                : skillKeywords.entrySet()
        ) {
            String skill =
                    skillEntry.getKey();

            List<String> keywords =
                    skillEntry.getValue();

            List<String> evidenceRepositories =
                    new ArrayList<>();

            for (RepositoryResponse repo : repositories) {

                String language =
                        repo.getLanguage();

                String name =
                        repo.getName();

                String description =
                        repo.getDescription();

                boolean detected = false;
                String source = "";

                if (containsIgnoreCase(language, skill)) {
                    detected = true;
                    source = "repository language";
                }

                String repositoryName =
                        name == null
                                ? ""
                                : name.toLowerCase();

                String repositoryDescription =
                        description == null
                                ? ""
                                : description.toLowerCase();

                if (!detected) {
                    for (String keyword : keywords) {

                        String lowerKeyword =
                                keyword.toLowerCase();

                        if (
                                repositoryName.contains(
                                        lowerKeyword
                                )
                        ) {
                            detected = true;
                            source = "repository name";
                            break;
                        }

                        if (
                                repositoryDescription.contains(
                                        lowerKeyword
                                )
                        ) {
                            detected = true;
                            source =
                                    "repository description";
                            break;
                        }
                    }
                }

                if (detected) {
                    evidenceRepositories.add(
                            repo.getName()
                                    + " ("
                                    + source
                                    + ")"
                    );
                }
            }

            if (!evidenceRepositories.isEmpty()) {

                Map<String, Object> skillData =
                        new LinkedHashMap<>();

                skillData.put(
                        "skill",
                        skill
                );

                skillData.put(
                        "repositoryCount",
                        evidenceRepositories.size()
                );

                skillData.put(
                        "evidenceRepositories",
                        evidenceRepositories
                );

                skillData.put(
                        "evidence",
                        evidenceRepositories.size()
                                + " "
                                + (
                                evidenceRepositories.size() == 1
                                        ? "repository"
                                        : "repositories"
                        )
                                + " provide evidence for "
                                + skill
                );

                skillEvidence.add(
                        skillData
                );
            }
        }

        skillEvidence.sort(
                Comparator.comparingInt(
                        item ->
                                -((Integer) item.get(
                                        "repositoryCount"
                                ))
                )
        );

        List<Map<String, Object>> limitedSkillEvidence =
                skillEvidence;

        if (skillEvidence.size() > 12) {
            limitedSkillEvidence =
                    new ArrayList<>(
                            skillEvidence.subList(
                                    0,
                                    12
                            )
                    );
        }

        /*
         * =====================================================
         * RESPONSE DATA
         * =====================================================
         */

        insights.put(
                "totalRepositories",
                totalRepositories
        );

        insights.put(
                "totalStars",
                totalStars
        );

        insights.put(
                "totalForks",
                totalForks
        );

        insights.put(
                "languages",
                languageCounts
        );

        insights.put(
                "primaryLanguage",
                primaryLanguage
        );

        insights.put(
                "mostPopularRepository",
                mostPopularRepository
        );

        insights.put(
                "mostPopularRepositoryStars",
                mostPopularRepositoryStars
        );

        insights.put(
                "topRepositories",
                topRepositories
        );

        insights.put(
                "languagePercentages",
                languagePercentages
        );

        /*
         * =====================================================
         * GITHEALTH SCORE
         * =====================================================
         */

        insights.put(
                "gitHealthScore",
                gitHealthScore
        );

        insights.put(
                "repositoryScore",
                repositoryScore
        );

        insights.put(
                "starScore",
                starScore
        );

        insights.put(
                "forkScore",
                forkScore
        );

        insights.put(
                "languageScore",
                languageScore
        );

        insights.put(
                "popularityScore",
                popularityScore
        );

        /*
         * =====================================================
         * ACTIVITY
         * =====================================================
         */

        insights.put(
                "recentActivityCount",
                recentActivityCount
        );

        insights.put(
                "activityLevel",
                activityLevel
        );

        insights.put(
                "recentRepositories",
                recentRepositoryData
        );

        /*
         * =====================================================
         * QUALITY
         * =====================================================
         */

        insights.put(
                "averageQualityScore",
                averageQualityScore
        );

        insights.put(
                "overallQualityLevel",
                overallQualityLevel
        );

        insights.put(
                "excellentRepositories",
                excellentRepositories
        );

        insights.put(
                "goodRepositories",
                goodRepositories
        );

        insights.put(
                "moderateRepositories",
                moderateRepositories
        );

        insights.put(
                "basicRepositories",
                basicRepositories
        );

        /*
         * =====================================================
         * SKILLS
         * =====================================================
         */

        insights.put(
                "skillEvidence",
                limitedSkillEvidence
        );

        return insights;
    }

    private boolean containsIgnoreCase(
            String value,
            String skill
    ) {
        if (value == null || value.isBlank()) {
            return false;
        }

        return value.equalsIgnoreCase(skill);
    }
}