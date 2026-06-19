package ru.polymetal.labManufacture.service.nextcloud.Impl;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import ru.polymetal.labManufacture.config.NextcloudConfig;
import ru.polymetal.labManufacture.data.models.Device;
import ru.polymetal.labManufacture.data.models.DeviceSubType;
import ru.polymetal.labManufacture.data.repository.DeviceRepository;
import ru.polymetal.labManufacture.data.repository.DeviceSubTypeRepository;
import ru.polymetal.labManufacture.service.nextcloud.LinkService;
import ru.polymetal.labManufacture.service.nextcloud.NextcloudService;
import java.io.IOException;
import java.util.Base64;
import org.springframework.scheduling.annotation.Async;
import ru.polymetal.labManufacture.service.nextcloud.ShareCallback;


@Service
@Slf4j
@RequiredArgsConstructor
public class LinkServiceImpl implements LinkService {


    private final NextcloudConfig nextcloudConfig;
    private final DeviceRepository deviceRepository;
    private final DeviceSubTypeRepository deviceSubTypeRepository;

    private final NextcloudService nextcloudService;



    @Override
    @Transactional
    public void createPublicShare(String sn, String filePath, String shareName, String apiUrl,
                                  Device device, Integer access) throws IOException {


// Формируем тело запроса (application/x-www-form-urlencoded)


        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("path", filePath);  // Убедитесь, что путь правильный
        body.add("shareType", "3");
        body.add("permissions", String.valueOf(access));

        if (shareName != null && !shareName.isEmpty()) {
            body.add("name", shareName);
        }

// Создаем заголовки
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("OCS-APIRequest", "true");
        headers.set("User-Agent", "MyApp/1.0");

        headers.set("Cache-Control", "no-cache, no-store, max-age=0, must-revalidate");
        headers.set("Pragma", "no-cache");
        headers.set("Expires", "0");
        headers.set("If-None-Match", "*");
        headers.set("If-Modified-Since", "Thu, 01 Jan 1970 00:00:00 GMT");

// Дополнительные для полного запрета кэша
        headers.set("Cache-Control", "no-cache, no-store, must-revalidate, private");
        headers.set("Surrogate-Control", "no-store");
        headers.set("X-Accel-Expires", "0");  // для nginx
        headers.set("Last-Modified", "Thu, 01 Jan 1970 00:00:00 GMT");

// Устанавливаем базовую аутентификацию
        String auth = nextcloudConfig.getUsername() + ":" + nextcloudConfig.getPassword();
        byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes());
        String authHeader = "Basic " + new String(encodedAuth);
        headers.set("Authorization", authHeader);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        log.debug("Creating share with params: {}", body);
        log.debug("URL: {}", apiUrl);
        log.debug("Headers: {}", headers);

        try {
            // Важно: используем FormHttpMessageConverter
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.getMessageConverters().add(0, new FormHttpMessageConverter());

            ResponseEntity<JsonNode> response = restTemplate.exchange(
                    apiUrl,
                    HttpMethod.POST,
                    request,
                    JsonNode.class
            );

            log.debug("Response: {}", response.getBody());

            if (response.getStatusCode().is2xxSuccessful()) {
                JsonNode responseBody = response.getBody();

                // Проверяем статус ответа OCS
                if (responseBody != null && responseBody.has("ocs")) {
                    JsonNode ocs = responseBody.get("ocs");
                    JsonNode meta = ocs.get("meta");
                    String status = meta.get("status").asText();

                    if ("ok".equals(status)) {
                        // Извлекаем URL из JSON ответа
                        JsonNode data = ocs.get("data");
                        String shareUrl = data.get("url").asText();
                        log.info("Public share created: {}", shareUrl);
                        if(filePath.contains(".pdf")) {
                            if(access == 3 || access == 31) {
                                device.setUrlPDF(shareUrl);
                            }
                            if(access == 1){
                                device.setUrlPDFRead(shareUrl);
                            }
                            deviceRepository.save(device);
                        } else {
                            if(access == 3 || access == 31) {
                                device.setUrlTXT(shareUrl);
                            }
                            if(access == 1){
                                device.setUrlTXTRead(shareUrl);
                            }
                            deviceRepository.save(device);
                        }
                    } else {
                        String message = meta.get("message").asText();
                        throw new IOException("OCS API error: " + message);
                    }
                }
            }

            //throw new IOException("Failed to create share: " + response.getStatusCode());

        } catch (Exception e) {
            log.error("Error creating public share", e);
            throw new IOException("Failed to create share: " + e.getMessage(), e);
        }
    }



    @Override
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void createFile(String sn) throws IOException {


        Device device =
                deviceRepository.findOneBySerialNumberAndIsDeletedFalse(sn).orElseThrow(() -> new RuntimeException(
                        "Плата не найдена"));

        String newFileNamePdf = device.getSerialNumber() + ".pdf";
        String newFileNameTxt = device.getSerialNumber() + ".txt";


        boolean pdfExists = nextcloudService.fileExists(newFileNamePdf);
        boolean txtExists = nextcloudService.fileExists(newFileNameTxt);




        String apiUrl = nextcloudConfig.getPublicUrl() +
                "/ocs/v2.php/apps/files_sharing/api/v1/shares?format=json";

        String shareName = "ссылка";
        String filePathPdf = "/" + newFileNamePdf;
        String filePathTxt = "/" + newFileNameTxt;
        byte[] content = nextcloudService.downloadFile(device.getSubtype().getFileName());

        if (!pdfExists && content.length > 0) {
            nextcloudService.uploadFile(newFileNamePdf, content);
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            this.createPublicShare(sn, filePathPdf, shareName, apiUrl, device, 31);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("Sleep interrupted", e);
            }

            this.createPublicShare(sn, filePathPdf, shareName, apiUrl, device, 1);

            log.info("PDF file created: {}", newFileNamePdf);
        } else {
            log.info("PDF file already exists: {}, skipping upload", newFileNamePdf);
            // Если файл есть, но ссылки нет в БД - создаем только ссылку
            if (device.getUrlPDF() == null || device.getUrlPDF().isEmpty() ||
                    device.getUrlPDFRead() == null || device.getUrlPDFRead().isEmpty()) {

                this.createPublicShare(sn, filePathPdf, shareName, apiUrl, device, 31);

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.warn("Sleep interrupted", e);
                }
                this.createPublicShare(sn, filePathPdf, shareName, apiUrl, device, 1);

            }
        }

        // Создаем TXT файл только если его нет
        if (!txtExists) {
            byte[] emptyContent = new byte[0];
            nextcloudService.uploadFile(newFileNameTxt, emptyContent);
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            this.createPublicShare(sn, filePathTxt, shareName, apiUrl, device, 31);

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("Sleep interrupted", e);
            }

            this.createPublicShare(sn, filePathTxt, shareName, apiUrl, device, 1);

            log.info("TXT file created: {}", newFileNameTxt);
        } else {
            log.info("TXT file already exists: {}, skipping upload", newFileNameTxt);
            if (device.getUrlTXT() == null || device.getUrlTXT().isEmpty() ||
                    device.getUrlTXTRead() == null || device.getUrlTXTRead().isEmpty()) {
                this.createPublicShare(sn, filePathTxt, shareName, apiUrl, device, 31);

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.warn("Sleep interrupted", e);
                }

                this.createPublicShare(sn, filePathTxt, shareName, apiUrl, device, 1);

            }
        }

    }


    @Override
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void createPublicShareDeviceSubType(String name,
                                  DeviceSubType deviceSubType) throws IOException {
//        DeviceSubType fresh =
//                deviceSubTypeRepository.findById(deviceSubType.getId()).orElseThrow(() -> new RuntimeException(
//                        "Тип не найден"));

        boolean pdfExists = nextcloudService.fileExists(name);

        if(pdfExists) {
            String filePath = "/" + name;
            String apiUrl = nextcloudConfig.getPublicUrl() +
                    "/ocs/v2.php/apps/files_sharing/api/v1/shares?format=json";
            String shareName = "ссылка";

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();

            body.add("path", filePath);  // Убедитесь, что путь правильный
            body.add("shareType", "3");
            body.add("permissions", "31");

            if (shareName != null && !shareName.isEmpty()) {
                body.add("name", shareName);
            }

// Создаем заголовки
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.set("OCS-APIRequest", "true");
            headers.set("User-Agent", "MyApp/1.0");

            headers.setCacheControl("no-cache, no-store, must-revalidate");
            headers.setPragma("no-cache");
            headers.setExpires(0);
            headers.set("Cache-Control", "no-cache, no-store, must-revalidate");
            headers.set("Pragma", "no-cache");

// Устанавливаем базовую аутентификацию
            String auth = nextcloudConfig.getUsername() + ":" + nextcloudConfig.getPassword();
            byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes());
            String authHeader = "Basic " + new String(encodedAuth);
            headers.set("Authorization", authHeader);

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

            log.debug("Creating share with params: {}", body);
            log.debug("URL: {}", apiUrl);
            log.debug("Headers: {}", headers);

            try {
                // Важно: используем FormHttpMessageConverter
                RestTemplate restTemplate = new RestTemplate();
                restTemplate.getMessageConverters().add(0, new FormHttpMessageConverter());

                ResponseEntity<JsonNode> response = restTemplate.exchange(
                        apiUrl,
                        HttpMethod.POST,
                        request,
                        JsonNode.class
                );

                log.debug("Response: {}", response.getBody());

                if (response.getStatusCode().is2xxSuccessful()) {
                    JsonNode responseBody = response.getBody();

                    // Проверяем статус ответа OCS
                    if (responseBody != null && responseBody.has("ocs")) {
                        JsonNode ocs = responseBody.get("ocs");
                        JsonNode meta = ocs.get("meta");
                        String status = meta.get("status").asText();

                        if ("ok".equals(status)) {
                            // Извлекаем URL из JSON ответа
                            JsonNode data = ocs.get("data");
                            String shareUrl = data.get("url").asText();
                            log.info("Public share created: {}", shareUrl);
                            if (filePath.contains(".pdf")) {
                                deviceSubType.setUrlPDF(shareUrl);
                                deviceSubTypeRepository.save(deviceSubType);
                            }
                        } else {
                            String message = meta.get("message").asText();
                            throw new IOException("OCS API error: " + message);
                        }
                    }
                }

            } catch (Exception e) {
                log.error("Error creating public share", e);
                throw new IOException("Failed to create share: " + e.getMessage(), e);
            }
        }
    }

}
