package ru.polymetal.labManufacture.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.polymetal.labManufacture.data.models.Device;
import ru.polymetal.labManufacture.data.models.DeviceSubType;
import ru.polymetal.labManufacture.data.repository.DeviceRepository;
import ru.polymetal.labManufacture.data.repository.DeviceSubTypeRepository;
import ru.polymetal.labManufacture.service.DeviceSubTypeService;
import ru.polymetal.labManufacture.service.nextcloud.LinkService;
import ru.polymetal.labManufacture.service.nextcloud.NextcloudService;
import java.io.IOException;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class LinkServiceScheduler {
    public final DeviceRepository deviceRepository;
    public final DeviceSubTypeRepository deviceSubTypeRepository;
    public final NextcloudService nextcloudService;
    public final DeviceSubTypeService deviceSubTypeService;
    public final LinkService linkService;

   @Scheduled(cron = "0 0 1,6 * * *")
   public void taskWithCron() throws IOException {
        log.info("Запущена синхронизация файлов устройств с Nextcloud");
        List<Device> devices = deviceRepository.findAll();
        int requested = 0;

        for(Device d: devices) {
            if(d.getUrlPDF() == null || d.getUrlPDF().isEmpty()) {
                linkService.createFile(d.getSerialNumber());
                requested++;
            }
        }

       for(Device d: devices) {
           if(d.getUrlTXT() == null || d.getUrlTXT().isEmpty()) {
               linkService.createFile(d.getSerialNumber());
               requested++;
           }
       }
       log.info("Синхронизация файлов устройств завершена: devices={}, requests={}", devices.size(), requested);
    }


    @Scheduled(cron = "0 0 2,7 * * *")
    public void taskWithCronTwo() throws IOException {
        log.info("Запущена синхронизация публичных ссылок типов плат");
        List<DeviceSubType> deviceSubType = deviceSubTypeRepository.findAll();
        int requested = 0;


        for(DeviceSubType dst: deviceSubType) {
            if(nextcloudService.fileExists(dst.getFileName())){
                if(dst.getUrlPDF() == null || dst.getUrlPDF().isEmpty()) {
                    linkService.createPublicShareDeviceSubType(dst.getFileName(), dst);
                    requested++;
                }
            }
        }
        log.info("Синхронизация ссылок типов плат завершена: subtypes={}, requests={}",
                deviceSubType.size(), requested);
    }

}
