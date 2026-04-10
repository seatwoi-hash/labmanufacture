package ru.polymetal.labManufacture.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.polymetal.labManufacture.data.models.Device;
import ru.polymetal.labManufacture.data.repository.DeviceRepository;
import ru.polymetal.labManufacture.service.nextcloud.LinkService;
import java.io.IOException;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class LinkServiceScheduler {
    public final DeviceRepository deviceRepository;
    public final LinkService linkService;

    @Scheduled(cron = "0 */10 * * * *")
    public void taskWithCron() throws IOException {
        List<Device> devices = deviceRepository.findAll();

        for(Device d: devices) {
            if(d.getUrlPDF().isEmpty()) {
                linkService.createFile(d.getSerialNumber());
            }
        }
    }

}
