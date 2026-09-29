package com.githealth.githealth.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class GitHealthController {

    @GetMapping("/")
    public String home() {
        return "index";
    }
}