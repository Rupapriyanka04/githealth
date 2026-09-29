package com.githealth.githealth.dto;

public class GitHubProfileResponse {

    private String login;
    private String name;
    private String avatarUrl;
    private String htmlUrl;
    private int publicRepos;
    private int followers;
    private int following;
    private String location;
    private String bio;

    public GitHubProfileResponse() {
    }

    public GitHubProfileResponse(String login, String name, String avatarUrl,
                                 String htmlUrl, int publicRepos, int followers,
                                 int following, String location, String bio) {
        this.login = login;
        this.name = name;
        this.avatarUrl = avatarUrl;
        this.htmlUrl = htmlUrl;
        this.publicRepos = publicRepos;
        this.followers = followers;
        this.following = following;
        this.location = location;
        this.bio = bio;
    }

    public String getLogin() {
        return login;
    }

    public String getName() {
        return name;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public String getHtmlUrl() {
        return htmlUrl;
    }

    public int getPublicRepos() {
        return publicRepos;
    }

    public int getFollowers() {
        return followers;
    }

    public int getFollowing() {
        return following;
    }

    public String getLocation() {
        return location;
    }

    public String getBio() {
        return bio;
    }
}