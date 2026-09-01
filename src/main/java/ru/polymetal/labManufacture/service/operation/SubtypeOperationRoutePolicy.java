package ru.polymetal.labManufacture.service.operation;

import org.springframework.stereotype.Component;
import ru.polymetal.labManufacture.data.models.DeviceSubType;

import java.util.Set;

/**
 * Проверяет доступность производственного этапа по настройкам типа платы.
 *
 * @author Tatarinov Anton
 */
@Component
public class SubtypeOperationRoutePolicy {

    private static final Set<String> SIDE_TWO_OPERATIONS = Set.of(
            "Монтаж \"Сторона 2\"");
    private static final Set<String> INSTALLATION_ONE_OPERATIONS = Set.of(
            "Выводной монтаж №1", "ОТК №2", "Ремонт №2", "ОТК №2.1");
    private static final Set<String> TEST_TWO_OPERATIONS = Set.of(
            "Выводной монтаж №2", "ОТК №4", "Ремонт №4", "ОТК №4.1",
            "Тестировка №2", "Диагностика №2", "Ремонт №5", "ОТК №4.2");

    public boolean isAllowed(DeviceSubType subtype, String operationName) {
        if (subtype == null || operationName == null || operationName.isBlank()) {
            return true;
        }
        if (Boolean.FALSE.equals(subtype.getIsSideTwo())
                && SIDE_TWO_OPERATIONS.contains(operationName)) {
            return false;
        }
        if (Boolean.FALSE.equals(subtype.getIsInstallationOne())
                && INSTALLATION_ONE_OPERATIONS.contains(operationName)) {
            return false;
        }
        return !Boolean.FALSE.equals(subtype.getIsTestTwo())
                || !TEST_TWO_OPERATIONS.contains(operationName);
    }
}
