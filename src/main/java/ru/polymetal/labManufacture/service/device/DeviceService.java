package ru.polymetal.labManufacture.service.device;

import ru.polymetal.labManufacture.data.models.Account;
import ru.polymetal.labManufacture.data.models.Device;
import ru.polymetal.labManufacture.data.models.DeviceSubType;
import ru.polymetal.labManufacture.data.models.Operation;
import ru.polymetal.labManufacture.dto.DeviceDto;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * Контракт сервиса DeviceService.
 *
 * @author Tatarinov Anton
 */
public interface DeviceService {
    void createDevice(DeviceDto deviceDto, String username) throws IOException;
    boolean existsSerialNumber(String sn);
    void validateDeviceDto(DeviceDto deviceDto);
    void delete(UUID id);
    List<Device> findAll();

}
