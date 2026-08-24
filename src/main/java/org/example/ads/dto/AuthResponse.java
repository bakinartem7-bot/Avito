package org.example.ads.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class AuthResponse {
    @Schema(description = "Токен доступа (JWT)", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6...")
    private String accessToken;

    @Schema(description = "Токен обновления (опционально)", example = "refresh-token-example")
    private String refreshToken;
}
