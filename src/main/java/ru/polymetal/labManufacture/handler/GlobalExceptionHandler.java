package ru.polymetal.labManufacture.handler;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import ru.polymetal.labManufacture.exception.DeviceTypeNotFoundException;
import ru.polymetal.labManufacture.exception.OperationNotFoundException;
import ru.polymetal.labManufacture.exception.UserNotFoundException;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(UserNotFoundException.class)
    public String handleUserNotFound(UserNotFoundException ex, Model model) {
        model.addAttribute("error", ex.getMessage());
        return "redirect:/login";
    }

    @ExceptionHandler(OperationNotFoundException.class)
    public String handleOperationNotFound(OperationNotFoundException ex, Model model) {
        model.addAttribute("error", ex.getMessage());
        return "redirect:/main";
    }

    @ExceptionHandler(DeviceTypeNotFoundException.class)
    public String handleDeviceTypeNotFound(DeviceTypeNotFoundException ex, Model model) {
        model.addAttribute("error", ex.getMessage());
        return "redirect:/main";
    }

}
