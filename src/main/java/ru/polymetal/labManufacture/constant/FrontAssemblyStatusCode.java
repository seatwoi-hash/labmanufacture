package ru.polymetal.labManufacture.constant;

public enum FrontAssemblyStatusCode {
    CREATED("created"),
    QUALITY_CHECK_1("Quality_check_1"),
    FAIL_QUALITY_CHECK_1("Fail_quality_check_1"),
    REPAIR_1("Repair1"),
    TEST_1("Test1"),
    FAIL_TEST_1("Fail_test_1"),
    DIAGNOSTICS("Diagnostician"),
    QUALITY_CHECK_2("Quality_check_2"),
    FAIL_QUALITY_CHECK_2("Fail_quality_check_2"),
    REPAIR_2("Repair2"),
    TEST_2("Test2"),
    FAIL_TEST_2("Fail_test_2"),
    QUALITY_CHECK_3("Quality_check_3"),
    FAIL_QUALITY_CHECK_3("Fail_quality_check_3"),
    READY("ready");

    private final String code;

    FrontAssemblyStatusCode(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
