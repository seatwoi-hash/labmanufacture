package ru.polymetal.labManufacture.service.nextcloud.Impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.sardine.Sardine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.Document;
import ru.polymetal.labManufacture.config.NextcloudConfig;
import ru.polymetal.labManufacture.service.nextcloud.NextcloudService;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import org.w3c.dom.NodeList;
import org.springframework.util.MultiValueMap;

@Service
@Slf4j
@RequiredArgsConstructor
public class NextcloudServiceImpl implements NextcloudService {

    private final Sardine sardine;

    private final String webdavBaseUrl;

    private final NextcloudConfig nextcloudConfig;

    private final RestTemplate restTemplate;

    @Override
    public void uploadFile(String remotePath, byte[] content) throws IOException {

        String fullPath = buildFullPath(remotePath);

        String parentDir = fullPath.substring(0, fullPath.lastIndexOf("/"));
        if (!sardine.exists(parentDir)) {
            log.info("Creating directory: {}", parentDir);
            sardine.createDirectory(parentDir);
        }

        sardine.put(fullPath, content);

    }

    @Override
    public void deleteFile(String remotePath) throws IOException {
        String fullPath = buildFullPath(remotePath);

        sardine.delete(fullPath);

    }

    @Override
    public byte[] downloadFile(String remotePath) throws IOException {
        String fullPath = buildFullPath(remotePath);

        if (!sardine.exists(fullPath)) {
            throw new IOException("File not found: " + fullPath);
        }

        try (InputStream inputStream = sardine.get(fullPath)) {
            byte[] content = inputStream.readAllBytes();
            log.info("File downloaded: {} ({} bytes)", fullPath, content.length);
            return content;
        }
    }

    private String buildFullPath(String remotePath) {
        String cleanPath = remotePath.startsWith("/") ? remotePath.substring(1) : remotePath;

        return webdavBaseUrl + cleanPath;
    }

    public void createDirectory(String remotePath) throws IOException {
            String fullPath = buildFullPath(remotePath);

            sardine.createDirectory(fullPath);

    }

    @Override
    public boolean fileExists(String fileName) throws IOException {
        try {
            String fileUrl = webdavBaseUrl + "/" + fileName;
            // Sardine имеет метод exists
            return sardine.exists(fileUrl);
        } catch (Exception e) {
            log.error("Error checking if file exists: {}", fileName, e);
            return false;
        }
    }

    private String extractShareUrlFromResponse(String response) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new ByteArrayInputStream(response.getBytes()));

            // Ищем элемент url
            NodeList urlNodes = doc.getElementsByTagName("url");
            if (urlNodes.getLength() > 0) {
                return urlNodes.item(0).getTextContent();
            }

            // Ищем элемент token
            NodeList tokenNodes = doc.getElementsByTagName("token");
            if (tokenNodes.getLength() > 0) {
                String token = tokenNodes.item(0).getTextContent();
                return nextcloudConfig.getServerUrl() + "/s/" + token;
            }

        } catch (Exception e) {
            log.error("Error parsing share response", e);
        }
        return null;
    }

}
