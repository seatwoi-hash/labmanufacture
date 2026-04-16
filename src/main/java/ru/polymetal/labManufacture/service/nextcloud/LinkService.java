package ru.polymetal.labManufacture.service.nextcloud;

import ru.polymetal.labManufacture.data.models.Device;
import ru.polymetal.labManufacture.data.models.DeviceSubType;
import ru.polymetal.labManufacture.service.nextcloud.Impl.LinkServiceImpl;
import java.io.IOException;


public interface LinkService {

    void createPublicShare(String sn, String filePath, String shareName, String apiUrl, Device device) throws IOException;

    void createFile(String sn) throws IOException;
    void createPublicShareDeviceSubType(String name,
                                        DeviceSubType deviceSubType) throws IOException;
}
