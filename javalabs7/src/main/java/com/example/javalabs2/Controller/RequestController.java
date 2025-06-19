package com.example.javalabs2.Controller;

import com.example.javalabs2.Service.RequestCounter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RequestController {

    private final RequestCounter requestCounter;

    @Autowired
    public RequestController(RequestCounter requestCounter) {
        this.requestCounter = requestCounter;
    }

    @GetMapping("/counter")
    public String getRequestCount() {
        return "Total requests: " + requestCounter.getCount();
    }

    @PostMapping("/counter/reset")
    public String resetRequestCount() {
        requestCounter.reset();
        return "Counter has been reset to 0";
    }
}