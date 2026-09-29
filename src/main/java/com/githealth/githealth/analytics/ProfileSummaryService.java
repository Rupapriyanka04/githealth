package com.githealth.githealth.analytics;

import com.githealth.githealth.dto.RepositoryResponse;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ProfileSummaryService {

    public Map<String, Object> generateSummary(
            List<RepositoryResponse> repositories) {

        Map<String, Object> summary = new LinkedHashMap<>();

        int totalRepositories = repositories.size();
        int totalStars = 0;
        int totalForks = 0;

        Map<String, Integer> languages = new LinkedHashMap<>();

        for (RepositoryResponse repo : repositories) {
            totalStars += repo.getStars();
            totalForks += repo.getForks();

            String language = repo.getLanguage();

            if (language != null && !language.isBlank()) {
                languages.put(
                        language,
                        languages.getOrDefault(language, 0) + 1
                );
            }
        }

        String primaryLanguage = "Not detected";
        int primaryLanguageCount = 0;

        for (Map.Entry<String, Integer> entry : languages.entrySet()) {
            if (entry.getValue() > primaryLanguageCount) {
                primaryLanguage = entry.getKey();
                primaryLanguageCount = entry.getValue();
            }
        }

        int languageScore = Math.min(languages.size() * 5, 15);
        int repositoryScore = Math.min(totalRepositories * 5, 25);
        int starScore = Math.min(totalStars / 100, 25);
        int forkScore = Math.min(totalForks / 100, 20);

        int mostStars = 0;

        for (RepositoryResponse repo : repositories) {
            mostStars = Math.max(mostStars, repo.getStars());
        }

        int popularityScore = Math.min(mostStars / 1000, 15);

        int gitHealthScore =
                repositoryScore +
                starScore +
                forkScore +
                languageScore +
                popularityScore;

        String activityLevel;

        if (totalRepositories >= 20) {
            activityLevel = "Very Active";
        } else if (totalRepositories >= 10) {
            activityLevel = "Active";
        } else if (totalRepositories >= 5) {
            activityLevel = "Moderately Active";
        } else {
            activityLevel = "Growing";
        }

        summary.put("gitHealthScore", gitHealthScore);
        summary.put("totalRepositories", totalRepositories);
        summary.put("totalStars", totalStars);
        summary.put("totalForks", totalForks);
        summary.put("primaryLanguage", primaryLanguage);
        summary.put("languageCount", languages.size());
        summary.put("activityLevel", activityLevel);
        summary.put("profileFocus", getProfileFocus(languages));

        return summary;
    }

    private String getProfileFocus(
            Map<String, Integer> languages) {

        if (languages.containsKey("Java")) {
            return "Java Development";
        }

        if (languages.containsKey("Python")) {
            return "Python Development";
        }

        if (languages.containsKey("JavaScript")) {
            return "Web Development";
        }

        if (languages.containsKey("TypeScript")) {
            return "Frontend Development";
        }

        if (languages.containsKey("C++")) {
            return "Systems & Competitive Programming";
        }

        return "General Software Development";
    }
}