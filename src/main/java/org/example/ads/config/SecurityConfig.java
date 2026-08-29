package org.example.ads.config;

import org.example.ads.service.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Конфигурация безопасности Spring Security для API сайта объявлений.
 * <p>
 * Настраивает:
 * - отключение CSRF для REST API,
 * - CORS-политику для фронтенда,
 * - правила авторизации по URL-путям,
 * - аутентификацию через Basic Auth и кастомный UserDetailsService,
 * - кодировщик паролей (BCrypt).
 */
@Configuration
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    public SecurityConfig(CustomUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    /**
     * Определяет цепочку фильтров безопасности ({@link SecurityFilterChain}).
     * <p>
     * Настраивает правила доступа:
     * - открытые эндпоинты: регистрация, Swagger, H2-консоль,
     * - защищённые эндпоинты объявлений: требуют авторизации,
     * - админские операции: требуют роли ADMIN.
     *
     * @param http объект конфигурации HttpSecurity
     * @return сконфигурированная цепочка фильтров безопасности
     * @throws Exception при ошибке конфигурации
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .httpBasic(Customizer.withDefaults())
                .userDetailsService(userDetailsService)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/register").permitAll() // Регистрация открыта для всех
                        .requestMatchers("/swagger-ui/**", "/api-docs/**", "/v3/api-docs/**", "/swagger-resources/**").permitAll()
                        .requestMatchers("/h2-console/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/ads/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/ads/**").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/ads/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/ads/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/admin/ads/**").hasRole("ADMIN")
                        .requestMatchers("/api/users/me").authenticated()
                        .requestMatchers("/api/users/me/**").authenticated()
                        .anyRequest().denyAll()
                );
        return http.build();
    }

    /**
     * Конфигурирует источник CORS-настроек.
     * <p>
     * Разрешает запросы с localhost:3000 и localhost:8080,
     * поддерживает все основные HTTP-методы и заголовки,
     * разрешает передачу учётных данных (credentials).
     *
     * @return источник конфигурации CORS
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:3000", "http://localhost:8080"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true); // Важно для передачи Basic Auth в браузере

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * Предоставляет бин кодировщика паролей на основе алгоритма BCrypt.
     * Используется для хеширования паролей при регистрации пользователей.
     *
     * @return экземпляр PasswordEncoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
