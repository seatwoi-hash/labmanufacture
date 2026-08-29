package ru.polymetal.labManufacture.controller.auth;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.Optional;

/**
 * MVC-контроллер AuthController.
 *
 * @author Tatarinov Anton
 */
@Controller
public class AuthController {

    @GetMapping("/login")
    public String showLoginPage(@RequestParam(value = "error", required = false) String error,
                                @RequestParam(value = "logout", required = false) String logout,
                                Model model) {
        Optional.ofNullable(error)
                .ifPresent(e -> model.addAttribute("error", "Неверное имя пользователя или пароль"));

        Optional.ofNullable(logout)
                .ifPresent(l -> model.addAttribute("message", "Вы успешно вышли из системы"));

        return "auth/login";
    }

}
