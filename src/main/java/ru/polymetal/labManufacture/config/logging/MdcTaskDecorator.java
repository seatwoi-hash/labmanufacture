package ru.polymetal.labManufacture.config.logging;

import org.slf4j.MDC;
import org.springframework.core.task.TaskDecorator;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Компонент инфраструктуры логирования MdcTaskDecorator.
 *
 * @author Tatarinov Anton
 */
@Component
public class MdcTaskDecorator implements TaskDecorator {

    @Override
    public Runnable decorate(Runnable task) {
        Map<String, String> callerContext = MDC.getCopyOfContextMap();
        return () -> {
            Map<String, String> workerContext = MDC.getCopyOfContextMap();
            try {
                if (callerContext == null) {
                    MDC.clear();
                } else {
                    MDC.setContextMap(callerContext);
                }
                task.run();
            } finally {
                if (workerContext == null) {
                    MDC.clear();
                } else {
                    MDC.setContextMap(workerContext);
                }
            }
        };
    }
}
