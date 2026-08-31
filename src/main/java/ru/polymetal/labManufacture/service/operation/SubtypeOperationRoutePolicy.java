package ru.polymetal.labManufacture.service.operation;

import org.springframework.stereotype.Component;
import ru.polymetal.labManufacture.data.models.DeviceSubType;

import java.util.Set;

/**
 * Ограничивает этапы возврата настройками производственного маршрута подтипа платы.
 *
 * @author Tatarinov Anton
 */
@Component
public class SubtypeOperationRoutePolicy {

    private static final String SIDE_TWO = "Монтаж \"Сторона 2\"";
    private static final Set<String> INSTALLATION_ONE_BRANCH = Set.of(
            "Выводной монтаж №1",
            "ОТК №2",
            "Ремонт №2",
            "ОТК №2.1"
    );
    private static final Set<String> TEST_TWO_BRANCH = Set.of(
            "Выводной монтаж №2",
            "ОТК №4",
            "ОТК №4.1",
            "ОТК №4.2",
            "Тестировка №2",
            "Диагностика №2",
            "Ремонт №4",
            "Ремонт №5"
    );

    public boolean isAllowed(DeviceSubType subtype, String targetStageName) {
        if (subtype == null || targetStageName == null) {
            return false;
        }
        if (Boolean.FALSE.equals(subtype.getIsSideTwo()) && SIDE_TWO.equals(targetStageName)) {
            return false;
        }
        if (Boolean.FALSE.equals(subtype.getIsInstallationOne())
                && INSTALLATION_ONE_BRANCH.contains(targetStageName)) {
            return false;
        }
        return !Boolean.FALSE.equals(subtype.getIsTestTwo()) || !TEST_TWO_BRANCH.contains(targetStageName);
    }
}
