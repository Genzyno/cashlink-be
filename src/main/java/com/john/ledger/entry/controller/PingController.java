package com.john.ledger.entry.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Public health check so mobile apps can verify API URL and network (no auth).
 * GET {baseUrl}public/ping
 */
@RestController
@RequestMapping("public")
public class PingController {

    @GetMapping("/ping")
    public Map<String, Object> ping() {
        return Map.of(
                "status", "ok",
                "service", "my-ledger-be",
                "message", "Backend is reachable"
        );
    }
}
