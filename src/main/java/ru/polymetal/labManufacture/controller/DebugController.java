package ru.polymetal.labManufacture.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.ui.Model;


@RestController
public class DebugController {

    @GetMapping("/debug/user")
    public String debugUser(Authentication authentication) {
        if (authentication != null) {
            return "Username: " + authentication.getName() + "\n" +
                    "Authorities: " + authentication.getAuthorities() + "\n" +
                    "Has ADMIN role: " + hasAdminRole(authentication);
        }
        return "Not authenticated";
    }

    private boolean hasAdminRole(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
    }
}
