package ru.polymetal.labManufacture.config;

import com.github.sardine.Sardine;
import com.github.sardine.SardineFactory;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import java.util.Base64;

@Configuration
@ConfigurationProperties(prefix = "nextcloud")
@Data
public class NextcloudConfig {

    private String serverUrl;
    private String username;
    private String password;
    private String webdavPath;
    private String publicUrl;

    private SharesConfig shares = new SharesConfig();

    @Data
    public static class SharesConfig {

        private int defaultExpirationDays = 1000000365;
        private boolean allowEditing = true;
        private String defaultPermissions = "31";

    }

    @Bean
    public Sardine sardine() {
        return SardineFactory.begin(username, password);
    }

    @Bean
    public RestTemplate restTemplate() {

        RestTemplate restTemplate = new RestTemplate();

        // Добавляем интерцептор для базовой аутентификации
        restTemplate.getInterceptors().add((request, body, execution) -> {
            String auth = username + ":" + password;
            byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes());
            String authHeader = "Basic " + new String(encodedAuth);
            request.getHeaders().add("Authorization", authHeader);
            return execution.execute(request, body);
        });

        return restTemplate;
    }


    @Bean
    public String webdavBaseUrl() {

        String baseUrl = serverUrl;
        if (!baseUrl.endsWith("/")) {
            baseUrl += "/";
        }

        String path = webdavPath;
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        if (!path.endsWith("/")) {
            path += "/";
        }

        return baseUrl + path;
    }


    public String getOcsApiUrl() {

        String baseUrl = serverUrl;
        if (!baseUrl.endsWith("/")) {
            baseUrl += "/";
        }
        return baseUrl + "ocs/v2.php";
    }
}
