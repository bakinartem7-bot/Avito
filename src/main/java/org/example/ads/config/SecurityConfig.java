package org.example.ads.config;

import org.example.ads.service.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
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
 * - отключение CSRF для REST API (так как используется stateless JWT-аутентификация),
 * - CORS-политику для взаимодействия с фронтендом,
 * - правила авторизации по URL-путям,
 * - кастомный механизм аутентификации через {@link CustomUserDetailsService},
 * - кодировщик паролей на основе BCrypt.
 * <p>
 * Для дипломной работы демонстрирует разделение открытых и защищённых эндпоинтов,
 * а также интеграцию Spring Security с собственной моделью пользователей.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    /**
     * Конструктор для внедрения зависимости {@link CustomUserDetailsService}.
     *
     * @param userDetailsService сервис загрузки данных пользователя для аутентификации
     */
    public SecurityConfig(CustomUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    /**
     * Определяет цепочку фильтров безопасности ({@link SecurityFilterChain}).
     * <p>
     * Настраивает правила доступа:
     * - открытые эндпоинты: регистрация, вход, Swagger, H2-консоль;
     * - защищённые эндпоинты объявлений: требуют валидного токена;
     * - админские операции: требуют роли ADMIN.
     * <p>
     * Важно: эндпоинт {@code /api/auth/login} должен быть доступен без предварительной
     * авторизации, иначе невозможно получить токен для последующих запросов.
     *
     * @param http объект конфигурации {@link HttpSecurity}
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

                        .requestMatchers("/api/auth/register", "/api/auth/login").permitAll()
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
     * Предоставляет бин {@link AuthenticationProvider} на основе DAO.
     * <p>
     * Связывает {@link CustomUserDetailsService} и {@link PasswordEncoder} для проверки
     * учётных данных пользователя при аутентификации.
     * Необходим для корректной работы эндпоинта {@code /login}.
     *
     * @return экземпляр {@link AuthenticationProvider}
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * Предоставляет менеджер аутентификации ({@link AuthenticationManager}).
     * <p>
     * Используется в {@link org.example.ads.controller.AuthController#login(AuthRequest)}
     * для проверки логина и пароля через Spring Security.
     *
     * @return экземпляр {@link AuthenticationManager}
     * @throws Exception при инициализации менеджера
     */
    @Bean
    public AuthenticationManager authenticationManager(
            org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
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
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * Предоставляет бин кодировщика паролей на основе алгоритма BCrypt.
     * <p>
     * Используется для хеширования паролей при регистрации пользователей
     * и проверки паролей во время аутентификации.
     *
     * @return экземпляр {@link PasswordEncoder}
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
