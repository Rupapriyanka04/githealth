package com.githealth.githealth.dto;

public class RepositoryResponse {

    private String name;
    private String description;
    private String language;
    private int stars;
    private int forks;
    private String htmlUrl;

    /*
     * Repository activity information
     */
    private String pushedAt;

    /*
     * Repository quality information
     */
    private int qualityScore;
    private String qualityLevel;

    public RepositoryResponse() {
    }

    public RepositoryResponse(
            String name,
            String description,
            String language,
            int stars,
            int forks,
            String htmlUrl,
            String pushedAt,
            int qualityScore,
            String qualityLevel) {

        this.name = name;
        this.description = description;
        this.language = language;
        this.stars = stars;
        this.forks = forks;
        this.htmlUrl = htmlUrl;
        this.pushedAt = pushedAt;
        this.qualityScore = qualityScore;
        this.qualityLevel = qualityLevel;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getLanguage() {
        return language;
    }

    public int getStars() {
        return stars;
    }

    public int getForks() {
        return forks;
    }

    public String getHtmlUrl() {
        return htmlUrl;
    }

    public String getPushedAt() {
        return pushedAt;
    }

    public int getQualityScore() {
        return qualityScore;
    }

    public String getQualityLevel() {
        return qualityLevel;
    }
}