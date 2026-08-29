package ru.polymetal.labManufacture.handler;

import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ui.Model;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import ru.polymetal.labManufacture.exception.DeviceTypeNotFoundException;
import ru.polymetal.labManufacture.exception.OperationNotFoundException;
import ru.polymetal.labManufacture.exception.UserNotFoundException;

import java.util.Map;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Map<String, Object>> handleMaxUploadSize(MaxUploadSizeExceededException ex) {
        log.warn("Загрузка отклонена: размер multipart-запроса превышает установленный лимит");
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(Map.of(
                "status", HttpStatus.PAYLOAD_TOO_LARGE.value(),
                "error", "Payload Too Large",
                "message", "Размер загружаемого файла превышает допустимый лимит"
        ));
    }

    @ExceptionHandler(UserNotFoundException.class)
    public String handleUserNotFound(UserNotFoundException ex, Model model) {
        log.warn("Пользователь не найден: {}", ex.getMessage());
        model.addAttribute("error", ex.getMessage());
        return "redirect:/login";
    }

    @ExceptionHandler(OperationNotFoundException.class)
    public String handleOperationNotFound(OperationNotFoundException ex, Model model) {
        log.warn("Операция не найдена: {}", ex.getMessage());
        model.addAttribute("error", ex.getMessage());
        return "redirect:/main";
    }

    @ExceptionHandler(DeviceTypeNotFoundException.class)
    public String handleDeviceTypeNotFound(DeviceTypeNotFoundException ex, Model model) {
        log.warn("Тип устройства не найден: {}", ex.getMessage());
        model.addAttribute("error", ex.getMessage());
        return "redirect:/main";
    }

    @ExceptionHandler(RuntimeException.class)
    public String handleRuntimeException(RuntimeException ex, HttpServletResponse response) {
        log.error("Необработанная ошибка приложения", ex);
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        return "404/404";
    }

    @ExceptionHandler(Exception.class)
    public String handleException(Exception ex, HttpServletResponse response) {
        log.error("Необработанная системная ошибка", ex);
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        return "404/404";
    }

}
