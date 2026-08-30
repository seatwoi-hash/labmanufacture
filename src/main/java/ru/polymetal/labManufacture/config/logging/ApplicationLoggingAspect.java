package ru.polymetal.labManufacture.config.logging;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import ru.polymetal.labManufacture.data.models.Account;
import ru.polymetal.labManufacture.data.models.Device;
import ru.polymetal.labManufacture.data.models.Operation;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/**
 * Компонент инфраструктуры логирования ApplicationLoggingAspect.
 *
 * @author Tatarinov Anton
 */
@Aspect
@Component
@Slf4j
public class ApplicationLoggingAspect {

    @Around("execution(public * ru.polymetal.labManufacture.controller..*(..))"
            + " || execution(public * ru.polymetal.labManufacture.service..*(..))"
            + " || execution(public * ru.polymetal.labManufacture.scheduler..*(..))")
    public Object logApplicationCall(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String operation = signature.getDeclaringType().getSimpleName() + "." + signature.getName();
        long startedAt = System.nanoTime();

        log.trace("Application operation arguments: operation={}, arguments={}",
                operation, summarizeArguments(signature.getParameterNames(), joinPoint.getArgs()));
        log.debug("Application operation started: operation={}", operation);
        if (isCommand(signature)) {
            log.info("Business command started: operation={}", operation);
        }
        try {
            Object result = joinPoint.proceed();
            if (isCommand(signature)) {
                log.info("Business command completed: operation={}, durationMs={}", operation, elapsedMs(startedAt));
            }
            log.debug("Application operation completed: operation={}, durationMs={}",
                    operation, elapsedMs(startedAt));
            log.trace("Application operation result: operation={}, result={}", operation, summarize(result));
            return result;
        } catch (Throwable error) {
            log.error("Application operation failed: operation={}, durationMs={}, errorType={}",
                    operation, elapsedMs(startedAt), error.getClass().getSimpleName(), error);
            throw error;
        }
    }

    private long elapsedMs(long startedAt) {
        return (System.nanoTime() - startedAt) / 1_000_000;
    }

    private boolean isCommand(MethodSignature signature) {
        return AnnotatedElementUtils.hasAnnotation(signature.getMethod(), PostMapping.class)
                || AnnotatedElementUtils.hasAnnotation(signature.getMethod(), PutMapping.class)
                || AnnotatedElementUtils.hasAnnotation(signature.getMethod(), PatchMapping.class)
                || AnnotatedElementUtils.hasAnnotation(signature.getMethod(), DeleteMapping.class);
    }

    private String summarizeArguments(String[] names, Object[] arguments) {
        StringBuilder result = new StringBuilder("{");
        for (int index = 0; index < arguments.length; index++) {
            if (index > 0) {
                result.append(", ");
            }
            String name = names != null && index < names.length ? names[index] : "arg" + index;
            result.append(name).append('=').append(summarizeArgument(name, arguments[index]));
        }
        return result.append('}').toString();
    }

    private boolean isSensitive(String name) {
        String normalized = name.toLowerCase();
        return normalized.contains("password") || normalized.contains("secret")
                || normalized.contains("token") || normalized.contains("description")
                || normalized.equals("data") || normalized.equals("bytes");
    }

    private String summarizeArgument(String name, Object value) {
        if (isSensitive(name)) {
            return "[REDACTED]";
        }
        if (value instanceof CharSequence text) {
            return isSafeTextParameter(name) ? text.toString() : "String{length=" + text.length() + "}";
        }
        return summarize(value);
    }

    private boolean isSafeTextParameter(String name) {
        String normalized = name.toLowerCase();
        return normalized.equals("sn") || normalized.equals("username") || normalized.equals("name")
                || normalized.equals("search") || normalized.contains("status");
    }

    private String summarize(Object value) {
        if (value == null) return "null";
        if (value instanceof MultipartFile file) {
            return "MultipartFile{name=" + file.getOriginalFilename() + ", size=" + file.getSize()
                    + ", contentType=" + file.getContentType() + "}";
        }
        if (value instanceof Authentication authentication) return "Authentication{name=" + authentication.getName() + "}";
        if (value instanceof Account account) return "Account{id=" + account.getId() + ", username=" + account.getUsername() + "}";
        if (value instanceof Device device) return "Device{id=" + device.getId() + ", sn=" + device.getSerialNumber() + "}";
        if (value instanceof Operation operation) return "Operation{id=" + operation.getId() + "}";
        if (value instanceof Page<?> page) return "Page{number=" + page.getNumber() + ", elements=" + page.getNumberOfElements() + "}";
        if (value instanceof Pageable pageable) return "Pageable{page=" + pageable.getPageNumber() + ", size=" + pageable.getPageSize() + "}";
        if (value instanceof Optional<?> optional) return "Optional{present=" + optional.isPresent() + "}";
        if (value instanceof Collection<?> collection) return value.getClass().getSimpleName() + "{size=" + collection.size() + "}";
        if (value instanceof Map<?, ?> map) return "Map{size=" + map.size() + "}";
        if (value instanceof CharSequence text) return "String{length=" + text.length() + "}";
        if (value instanceof Number || value instanceof Boolean
                || value instanceof Enum<?> || value instanceof java.util.UUID) return String.valueOf(value);
        return value.getClass().getSimpleName();
    }
}
