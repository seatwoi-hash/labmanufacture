package ru.polymetal.labManufacture.integration.testdata;

import ru.polymetal.labManufacture.data.models.Account;
import ru.polymetal.labManufacture.data.models.Device;
import ru.polymetal.labManufacture.data.models.DeviceSubType;
import ru.polymetal.labManufacture.data.models.DeviceType;
import ru.polymetal.labManufacture.data.models.Operation;
import ru.polymetal.labManufacture.data.models.OperationStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class DeviceTestData {

    public static Device createDevice(
            String serialNumber,
            DeviceType type,
            DeviceSubType subtype
    ) {
        Device device = new Device();
        device.setSubtype(subtype);
        device.setType(type);
        device.setSerialNumber(serialNumber);
        device.setCreatedTime(LocalDateTime.now());

        return device;
    }
}
