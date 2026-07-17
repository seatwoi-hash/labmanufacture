package ru.polymetal.labManufacture.integration.testdata;

import ru.polymetal.labManufacture.data.models.DeviceSubType;
import java.time.LocalDateTime;

public class DeviceSubTypeData {

    public static DeviceSubType createDeviceSubTypeData(String name,
                                                 Boolean isTestTwo,
                                                 Boolean isSideTwo,
                                                 Boolean isInstallationOne,
                                                 Integer snType,
                                                 Integer versionType) {


        DeviceSubType deviceSubType = new DeviceSubType();

        deviceSubType.setIsSideTwo(isTestTwo);
        deviceSubType.setIsSideTwo(isSideTwo);
        deviceSubType.setIsSideTwo(isInstallationOne);
        deviceSubType.setName(name);
        deviceSubType.setVersionType(versionType);
        deviceSubType.setSnType(snType);
        deviceSubType.setCreatedAt(LocalDateTime.now());
        deviceSubType.setDescription("Создан");


        return deviceSubType;
    }

    public static DeviceSubType createDeviceSubTypeData(String name) {

        DeviceSubType deviceSubType = new DeviceSubType();
        deviceSubType.setName(name);
        deviceSubType.setVersionType(22);
        deviceSubType.setSnType(2);
        deviceSubType.setDescription("Создан");
        deviceSubType.setCreatedAt(LocalDateTime.now());

        return deviceSubType;
    }
}
