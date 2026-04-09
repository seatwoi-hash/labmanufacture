package ru.polymetal.labManufacture.service.nextcloud.Impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.sardine.Sardine;
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
import ru.polymetal.labManufacture.data.repository.DeviceRepository;
import ru.polymetal.labManufacture.service.nextcloud.LinkService;
import ru.polymetal.labManufacture.service.nextcloud.NextcloudService;
import java.io.IOException;
import java.util.Base64;
import org.springframework.scheduling.annotation.Async;



@Service
@Slf4j
@RequiredArgsConstructor
public class LinkServiceImpl implements LinkService {

    private final Sardine sardine;

    private final String webdavBaseUrl;

    private final NextcloudConfig nextcloudConfig;
    private final DeviceRepository deviceRepository;
    private final NextcloudService nextcloudService;



    @Override
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void createPublicShare(String sn, String filePath, String shareName, String apiUrl, Device device) throws IOException {


// Формируем тело запроса (application/x-www-form-urlencoded)


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
                            device.setUrlPDF(shareUrl);
                            deviceRepository.save(device);
                        } else {
                            device.setUrlTXT(shareUrl);
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

        byte[] content = nextcloudService.downloadFile(device.getSubtype().getFileName());
        byte[] emptyContent = new byte[0];
        String newFileNamePdf = device.getSerialNumber() + ".pdf";
        String newFileNameTxt = device.getSerialNumber() + ".txt";
        nextcloudService.uploadFile(newFileNamePdf, content);
        nextcloudService.uploadFile(newFileNameTxt, emptyContent);

        String apiUrl = nextcloudConfig.getServerUrl() +
                "/ocs/v2.php/apps/files_sharing/api/v1/shares?format=json";

        String shareName = "ссылка";
        String filePathPdf = "/" + newFileNamePdf;
        String filePathTxt = "/" + newFileNameTxt;

        this.createPublicShare(sn, filePathPdf, shareName, apiUrl, device);
        this.createPublicShare(sn, filePathTxt, shareName, apiUrl, device);


    }





}
