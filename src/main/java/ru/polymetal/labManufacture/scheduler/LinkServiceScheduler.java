package ru.polymetal.labManufacture.scheduler;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.polymetal.labManufacture.data.models.Device;
import ru.polymetal.labManufacture.data.models.DeviceSubType;
import ru.polymetal.labManufacture.data.repository.DeviceRepository;
import ru.polymetal.labManufacture.data.repository.DeviceSubTypeRepository;
import ru.polymetal.labManufacture.service.nextcloud.LinkService;
import ru.polymetal.labManufacture.service.nextcloud.NextcloudService;

import java.io.IOException;
import java.util.List;

/**
 * Планировщик фоновых задач LinkServiceScheduler.
 *
 * @author Tatarinov Anton
 */
@Component
@RequiredArgsConstructor
public class LinkServiceScheduler {
    private final DeviceRepository deviceRepository;
    private final DeviceSubTypeRepository deviceSubTypeRepository;
    private final NextcloudService nextcloudService;
    private final LinkService linkService;

    @Scheduled(cron = "0 0 1,6 * * *")
    public void taskWithCron() throws IOException {
        List<Device> devices = deviceRepository.findAll();

        for (Device device : devices) {
            if (hasMissingLinks(device)) {
                linkService.createFile(device.getSerialNumber());
            }
        }
    }

    @Scheduled(cron = "0 0 2,7 * * *")
    public void taskWithCronTwo() throws IOException {
        List<DeviceSubType> deviceSubTypes = deviceSubTypeRepository.findAll();

        for (DeviceSubType deviceSubType : deviceSubTypes) {
            if (nextcloudService.fileExists(deviceSubType.getFileName())
                    && isBlank(deviceSubType.getUrlPDF())) {
                linkService.createPublicShareDeviceSubType(deviceSubType.getFileName(), deviceSubType);
            }
        }
    }

    private boolean hasMissingLinks(Device device) {
        return isBlank(device.getUrlPDF())
                || isBlank(device.getUrlPDFRead())
                || isBlank(device.getUrlTXT())
                || isBlank(device.getUrlTXTRead());
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
