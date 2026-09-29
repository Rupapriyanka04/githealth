package com.githealth.githealth.exception;

public class GitHubApiException extends RuntimeException {
    private final int statusCode;

    public GitHubApiException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}