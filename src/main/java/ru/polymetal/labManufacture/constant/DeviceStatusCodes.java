package ru.polymetal.labManufacture.constant;

import java.util.Arrays;
import java.util.Optional;

/**
 * Перечень кодов DeviceStatusCodes.
 *
 * @author Tatarinov Anton
 */
public enum DeviceStatusCodes {
    READY("ready"), CREATE("created"), SIDE1("Side1"), SIDE2("Side2"),
    INSTALLATION("Installation1"), INSTALLATION2("Installation2"),
    WASHING1("Washing1"), WASHING2("Washing2"),
    QUALITY_CHECK_1("Quality_check_№1"), QUALITY_CHECK_1_1("Quality_check_№1.1"),
    QUALITY_CHECK_2("Quality_check_№2"), QUALITY_CHECK_2_1("Quality_check_№2.1"),
    QUALITY_CHECK_3("Quality_check_№3"), QUALITY_CHECK_4("Quality_check_№4"),
    QUALITY_CHECK_4_1("Quality_check_№4.1"), QUALITY_CHECK_4_2("Quality_check_№4.2"),
    QUALITY_CHECK_5("Quality_check_№5"), QUALITY_CHECK_5_1("Quality_check_№5.1"),
    QUALITY_CHECK_5_1_1("Quality_check_№5.1.1"), QUALITY_CHECK_6("Quality_check_№6"),
    FAIL_QUALITY_CHECK_1("Fail_quality_check_№1"), FAIL_QUALITY_CHECK_1_1("Fail_quality_check_№1.1"),
    FAIL_QUALITY_CHECK_2("Fail_quality_check_№2"), FAIL_QUALITY_CHECK_2_1("Fail_quality_check_№2.1"),
    FAIL_QUALITY_CHECK_3("Fail_quality_check_№3"), FAIL_QUALITY_CHECK_4("Fail_quality_check_№4"),
    FAIL_QUALITY_CHECK_4_1("Fail_quality_check_№4.1"), FAIL_QUALITY_CHECK_4_2("Fail_quality_check_№4.2"),
    FAIL_QUALITY_CHECK_5("Fail_quality_check_№5"), FAIL_QUALITY_CHECK_5_1("Fail_quality_check_№5.1"),
    FAIL_QUALITY_CHECK_5_1_1("Fail_quality_check_№5.1.1"), FAIL_QUALITY_CHECK_6("Fail_quality_check_№6"),
    TEST("Test"), TEST_2("Test2"), FAIL_TEST("Fail_test"), FAIL_TEST_2("Fail_test2"),
    REPAIR1("Repair1"), REPAIR2("Repair2"), REPAIR3("Repair3"),
    REPAIR4("Repair4"), REPAIR5("Repair5"), REPAIR6("Repair6"),
    VARNISH("Varnish"), TECHNICAL("Technical"), TECHNICAL2("Technical2"), TECHNICAL3("Technical3"),
    DIAGNOSTICIAN_TEST_1("Diagnostician_check_test_№1"),
    DIAGNOSTICIAN_TEST_2("Diagnostician_check_test_№2"),
    DIAGNOSTICIAN_REPAIR_1("Diagnostician_check_repair_№1"),
    DIAGNOSTICIAN_REPAIR_2("Diagnostician_check_repair_№2"),
    NOT_READY("not_ready");

    private final String code;

    DeviceStatusCodes(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public boolean matches(String value) {
        return code.equals(value);
    }

    public static Optional<DeviceStatusCodes> fromCode(String code) {
        return Arrays.stream(values()).filter(status -> status.matches(code)).findFirst();
    }

    @Override
    public String toString() {
        return code;
    }
}
