package ru.polymetal.labManufacture.constant;

public enum FrontAssemblyStatusCode {
    CREATED("created"),
    QUALITY_CHECK_1("Quality_check_1"),
    REPAIR_1("Repair1"),
    TEST_1("Test1"),
    DIAGNOSTICS("Diagnostician"),
    QUALITY_CHECK_2("Quality_check_2"),
    REPAIR_2("Repair2"),
    TEST_2("Test2"),
    QUALITY_CHECK_3("Quality_check_3"),
    READY("ready");

    private final String code;

    FrontAssemblyStatusCode(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
