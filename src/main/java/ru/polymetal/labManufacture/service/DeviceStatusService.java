package ru.polymetal.labManufacture.service;

import ru.polymetal.labManufacture.data.models.OperationStatus;
import java.util.List;

/**
 * Контракт сервиса DeviceStatusService.
 *
 * @author Tatarinov Anton
 */
public interface DeviceStatusService {
    OperationStatus findByName(String name);
    public List<OperationStatus> findByListName(List<String> name);
}
