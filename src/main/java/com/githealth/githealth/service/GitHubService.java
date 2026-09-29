package com.githealth.githealth.service;

import com.githealth.githealth.dto.GitHubProfileResponse;
import com.githealth.githealth.dto.RepositoryResponse;
import com.githealth.githealth.exception.GitHubApiException;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class GitHubService {
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public GitHubService() {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    public GitHubProfileResponse getUser(String username) throws Exception {
        String url = "https://api.github.com/users/" + username;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/vnd.github+json")
                .header("User-Agent", "GitHealth")
                .GET()
                .build();

        HttpResponse<String> response =
                httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw createGitHubException(response);
        }

        JsonNode root = objectMapper.readTree(response.body());

        return new GitHubProfileResponse(
                getText(root, "login"),
                getText(root, "name"),
                getText(root, "avatar_url"),
                getText(root, "html_url"),
                getInt(root, "public_repos"),
                getInt(root, "followers"),
                getInt(root, "following"),
                getText(root, "location"),
                getText(root, "bio")
        );
    }

    public List<RepositoryResponse> getRepositories(String username) throws Exception {
        List<RepositoryResponse> repositories = new ArrayList<>();
        int page = 1;

        while (true) {
            String url = "https://api.github.com/users/" + username
                    + "/repos?per_page=100&page=" + page;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Accept", "application/vnd.github+json")
                    .header("User-Agent", "GitHealth")
                    .GET()
                    .build();

            HttpResponse<String> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw createGitHubException(response);
            }

            JsonNode root = objectMapper.readTree(response.body());

            if (!root.isArray() || root.isEmpty()) {
                break;
            }

            for (JsonNode repo : root) {
                String name = getText(repo, "name");
                String description = getText(repo, "description");
                String language = getText(repo, "language");
                int stars = getInt(repo, "stargazers_count");
                int forks = getInt(repo, "forks_count");
                String htmlUrl = getText(repo, "html_url");
                String pushedAt = getText(repo, "pushed_at");

                int qualityScore = calculateQualityScore(
                        description,
                        language,
                        stars,
                        forks
                );

                repositories.add(
                        new RepositoryResponse(
                                name,
                                description,
                                language,
                                stars,
                                forks,
                                htmlUrl,
                                pushedAt,
                                qualityScore,
                                getQualityLevel(qualityScore)
                        )
                );
            }

            if (root.size() < 100) {
                break;
            }

            page++;
        }

        repositories.sort(
                Comparator.comparing(
                        RepositoryResponse::getPushedAt,
                        Comparator.nullsLast(Comparator.reverseOrder())
                )
        );

        return repositories;
    }

    private int calculateQualityScore(
            String description,
            String language,
            int stars,
            int forks) {

        int score = 0;

        if (description != null && !description.isBlank()) {
            score += 25;
        }

        if (language != null && !language.isBlank()) {
            score += 25;
        }

        if (stars >= 100) {
            score += 25;
        } else if (stars >= 20) {
            score += 20;
        } else if (stars >= 5) {
            score += 15;
        } else if (stars >= 1) {
            score += 10;
        }

        if (forks >= 50) {
            score += 25;
        } else if (forks >= 10) {
            score += 20;
        } else if (forks >= 3) {
            score += 15;
        } else if (forks >= 1) {
            score += 10;
        }

        return score;
    }

    private String getQualityLevel(int score) {
        if (score >= 80) {
            return "Excellent";
        }

        if (score >= 60) {
            return "Good";
        }

        if (score >= 40) {
            return "Moderate";
        }

        return "Basic";
    }

    private GitHubApiException createGitHubException(
            HttpResponse<String> response) {

        int statusCode = response.statusCode();
        String message;

        switch (statusCode) {
            case 404:
                message = "GitHub user or resource was not found.";
                break;
            case 403:
                message = "GitHub API rate limit exceeded or access was forbidden.";
                break;
            case 401:
                message = "GitHub API authentication failed.";
                break;
            case 429:
                message = "Too many GitHub API requests. Please try again later.";
                break;
            case 500:
            case 502:
            case 503:
            case 504:
                message = "GitHub service is temporarily unavailable.";
                break;
            default:
                message = "GitHub API request failed.";
        }

        return new GitHubApiException(message, statusCode);
    }

    private String getText(JsonNode node, String field) {
        JsonNode value = node.get(field);

        if (value == null || value.isNull()) {
            return null;
        }

        return value.asText();
    }

    private int getInt(JsonNode node, String field) {
        JsonNode value = node.get(field);

        if (value == null || value.isNull()) {
            return 0;
        }

        return value.asInt();
    }
}