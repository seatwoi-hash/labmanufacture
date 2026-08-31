package ru.polymetal.labManufacture.unit;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import ru.polymetal.labManufacture.data.models.DeviceSubType;
import ru.polymetal.labManufacture.service.operation.SubtypeOperationRoutePolicy;

import static org.testng.Assert.assertEquals;

/**
 * Unit-тесты ограничений маршрута по настройкам подтипа платы.
 *
 * @author Tatarinov Anton
 */
public class SubtypeOperationRoutePolicyUnitTest {

    private final SubtypeOperationRoutePolicy policy = new SubtypeOperationRoutePolicy();

    @DataProvider
    public Object[][] routeCases() {
        return new Object[][]{
                {false, true, true, "Монтаж \"Сторона 2\"", false},
                {false, true, true, "ОТК №1", true},
                {true, false, true, "Выводной монтаж №1", false},
                {true, false, true, "ОТК №2", false},
                {true, false, true, "Ремонт №2", false},
                {true, false, true, "ОТК №2.1", false},
                {true, false, true, "Тестировка №1", true},
                {true, true, false, "Выводной монтаж №2", false},
                {true, true, false, "ОТК №4", false},
                {true, true, false, "Тестировка №2", false},
                {true, true, false, "Отмывка №1", true},
                {true, true, true, "Тестировка №2", true}
        };
    }

    @Test(dataProvider = "routeCases")
    public void filtersStagesBySubtypeFlags(boolean isSideTwo,
                                            boolean isInstallationOne,
                                            boolean isTestTwo,
                                            String stageName,
                                            boolean expected) {
        DeviceSubType subtype = DeviceSubType.builder()
                .isSideTwo(isSideTwo)
                .isInstallationOne(isInstallationOne)
                .isTestTwo(isTestTwo)
                .build();

        assertEquals(policy.isAllowed(subtype, stageName), expected);
    }
}
