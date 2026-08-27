package org.example.ads.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Конфигурация безопасности Spring Security БЕЗ JWT.
 * Использует стандартную аутентификацию Spring Security (Basic/Form).
 * Подходит для MVP и дипломной работы, если JWT не является обязательным требованием.
 */
@Configuration
public class SecurityConfig {

    /**
     * Основной бин цепочки безопасности (SecurityFilterChain).
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/api-docs/**").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-resources/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/ads/**").permitAll()
                        .requestMatchers(HttpMethod.DELETE, "/api/admin/ads/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
        ;

        return http.build();
    }

    /**
     * Источник конфигурации CORS.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:3000", "http://localhost:8080", "http://localhost"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * Бин PasswordEncoder для хеширования паролей пользователей.
     * Нужен даже без JWT, чтобы хранить пароли в БД.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
