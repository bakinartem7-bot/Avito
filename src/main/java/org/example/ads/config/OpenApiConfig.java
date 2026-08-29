package org.example.ads.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Конфигурация OpenAPI (Swagger) для API сайта объявлений.
 * <p>
 * Настраивает общую информацию об API (название, версия, описание),
 * а также схему безопасности на основе Bearer-токенов (JWT).
 * Добавляет в спецификацию эндпоинты Swagger UI и правила авторизации.
 */
@Configuration
public class OpenApiConfig {

    /**
     * Создаёт и настраивает экземпляр {@link OpenAPI} с метаданными и схемой безопасности.
     *
     * @return сконфигурированный объект OpenAPI
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Ads API")
                        .version("1.0.0")
                        .description("API для сайта объявлений (дипломный проект)")
                )
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .in(SecurityScheme.In.HEADER)
                                        .name("Authorization")
                        )
                );
    }
}
