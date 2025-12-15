package ru.polymetal.labManufacture.service;

import ru.polymetal.labManufacture.data.models.DeviceStatus;
import java.util.List;

public interface DeviceStatusService {
    DeviceStatus findByName(String name);
    public List<DeviceStatus> findByListName(List<String> name);
}
