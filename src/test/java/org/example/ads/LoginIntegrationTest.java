package org.example.ads;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class LoginIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void testLoginAndSeeRealError() {
        // Тело запроса
        var requestBody = Map.of(
                "email", "test@example.com",
                "password", "123456"
        );

        // Создаём заголовки отдельно — они будут изменяемыми
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Собираем HttpEntity: body + headers
        HttpEntity<Map<String, String>> request = new HttpEntity<>(requestBody, headers);

        // exchange вернёт ResponseEntity даже при 500, не выбрасывая исключение
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/auth/login",
                HttpMethod.POST,
                request,
                String.class
        );

        System.out.println("=== STATUS ===");
        System.out.println(response.getStatusCode());

        System.out.println("=== HEADERS ===");
        response.getHeaders().forEach((key, values) -> System.out.println(key + ": " + values));

        System.out.println("=== BODY ===");
        String body = response.getBody();
        System.out.println(body != null ? body : "(null)");

        assertNotNull(body, "Тело ответа должно быть, даже если это ошибка");
    }
}
