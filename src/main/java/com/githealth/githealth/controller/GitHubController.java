package com.githealth.githealth.controller;

import com.githealth.githealth.analytics.DeveloperInsights;
import com.githealth.githealth.analytics.ProfileSummaryService;
import com.githealth.githealth.analytics.RecommendationService;
import com.githealth.githealth.analytics.RoleAnalysisService;
import com.githealth.githealth.analytics.SkillRoadmapService;
import com.githealth.githealth.dto.GitHubProfileResponse;
import com.githealth.githealth.dto.RepositoryResponse;
import com.githealth.githealth.service.GitHubService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/github")
public class GitHubController {
    private final GitHubService gitHubService;
    private final DeveloperInsights developerInsights;
    private final RoleAnalysisService roleAnalysisService;
    private final RecommendationService recommendationService;
    private final SkillRoadmapService skillRoadmapService;
    private final ProfileSummaryService profileSummaryService;

    public GitHubController(
            GitHubService gitHubService,
            DeveloperInsights developerInsights,
            RoleAnalysisService roleAnalysisService,
            RecommendationService recommendationService,
            SkillRoadmapService skillRoadmapService,
            ProfileSummaryService profileSummaryService) {
        this.gitHubService = gitHubService;
        this.developerInsights = developerInsights;
        this.roleAnalysisService = roleAnalysisService;
        this.recommendationService = recommendationService;
        this.skillRoadmapService = skillRoadmapService;
        this.profileSummaryService = profileSummaryService;
    }

    @GetMapping("/{username}")
    public GitHubProfileResponse getUser(@PathVariable String username) throws Exception {
        return gitHubService.getUser(username);
    }

    @GetMapping("/{username}/repositories")
    public List<RepositoryResponse> getRepositories(@PathVariable String username) throws Exception {
        return gitHubService.getRepositories(username);
    }

    @GetMapping("/{username}/analytics")
    public Map<String, Object> getAnalytics(@PathVariable String username) throws Exception {
        List<RepositoryResponse> repositories = gitHubService.getRepositories(username);
        return developerInsights.analyze(repositories);
    }

    @GetMapping("/{username}/role-analysis")
    public Map<String, Object> analyzeTargetRole(
            @PathVariable String username,
            @RequestParam String role) throws Exception {
        List<RepositoryResponse> repositories = gitHubService.getRepositories(username);
        return roleAnalysisService.analyzeRole(role, repositories);
    }

    @GetMapping("/{username}/recommendations")
    public Map<String, Object> getRecommendations(
            @PathVariable String username,
            @RequestParam String role) throws Exception {
        List<RepositoryResponse> repositories = gitHubService.getRepositories(username);
        return recommendationService.generateRecommendations(role, repositories);
    }

    @GetMapping("/{username}/roadmap")
    public Map<String, Object> getRoadmap(
            @PathVariable String username,
            @RequestParam String role) throws Exception {
        List<RepositoryResponse> repositories = gitHubService.getRepositories(username);
        return skillRoadmapService.generateRoadmap(role, repositories);
    }

    @GetMapping("/{username}/summary")
    public Map<String, Object> getProfileSummary(
            @PathVariable String username) throws Exception {
        List<RepositoryResponse> repositories = gitHubService.getRepositories(username);
        return profileSummaryService.generateSummary(repositories);
    }

    @GetMapping("/roles")
    public List<String> getAvailableRoles() {
        return roleAnalysisService.getAvailableRoles();
    }
}