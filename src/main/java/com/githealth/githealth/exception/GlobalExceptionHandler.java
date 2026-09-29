package com.githealth.githealth.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(GitHubApiException.class)
    public ResponseEntity<Map<String,Object>> handleGitHubError(GitHubApiException ex) {
        Map<String,Object> error=new LinkedHashMap<>();
        error.put("error",ex.getMessage());
        error.put("status",ex.getStatusCode());

        HttpStatus status=HttpStatus.resolve(ex.getStatusCode());

        if(status==null) status=HttpStatus.BAD_GATEWAY;

        return ResponseEntity.status(status).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String,Object>> handleGeneralError(Exception ex) {
        Map<String,Object> error=new LinkedHashMap<>();
        error.put("error","Unable to complete the GitHub analysis.");
        error.put("status",500);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}