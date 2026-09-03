package ru.polymetal.labManufacture.controller.auth;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Проверка доступности текущей пользовательской сессии.
 */
@RestController
public class SessionController {

    @GetMapping("/session/heartbeat")
    public ResponseEntity<Void> heartbeat() {
        return ResponseEntity.noContent().build();
    }
}
