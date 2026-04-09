package ru.polymetal.labManufacture.service.nextcloud;

import ru.polymetal.labManufacture.data.models.Device;
import java.io.IOException;
import java.util.UUID;

public interface LinkService {

    void createPublicShare(String sn, String filePath, String shareName, String apiUrl, Device device) throws IOException;

    void createFile(String sn) throws IOException;
}
