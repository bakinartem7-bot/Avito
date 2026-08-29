package org.example.ads.controller;

import org.example.ads.exception.AccessDeniedException;
import org.example.ads.exception.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

/**
 * Глобальный обработчик исключений для REST API сайта объявлений.
 * <p>
 * Централизует обработку типовых ошибок приложения:
 * - NotFoundException → HTTP 404,
 * - AccessDeniedException → HTTP 403,
 * а также любых непредвиденных ошибок → HTTP 500.
 * Формирует структурированный JSON-ответ с кодом, сообщением и временем ошибки.
 * Такой подход повышает стабильность API и удобство отладки, а также улучшает UX клиента.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Обрабатывает исключение «ресурс не найден».
     * <p>
     * Возвращает HTTP-статус 404 и JSON с описанием ошибки.
     *
     * @param ex выброшенное исключение NotFoundException
     * @return ResponseEntity с телом ошибки и статусом 404
     */
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NotFoundException ex) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    /**
     * Обрабатывает исключение «отказ в доступе» (нарушение прав).
     * <p>
     * Возвращает HTTP-статус 403 и JSON с описанием ошибки.
     * Используется при попытке удалить/обновить чужой ресурс.
     *
     * @param ex выброшенное исключение AccessDeniedException
     * @return ResponseEntity с телом ошибки и статусом 403
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex) {
        return buildErrorResponse(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    /**
     * Обрабатывает любые непредвиденные исключения.
     * <p>
     * Возвращает HTTP-статус 500 и общее сообщение об ошибке.
     * Детали исключения не передаются клиенту в целях безопасности.
     *
     * @param ex любое необработанное исключение
     * @return ResponseEntity с телом ошибки и статусом 500
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Произошла внутренняя ошибка сервера");
    }

    /**
     * Формирует унифицированный объект ответа об ошибке.
     * <p>
     * Включает HTTP-статус, сообщение и временную метку.
     * Такой формат удобен для логирования и анализа инцидентов.
     *
     * @param status HTTP-статус ошибки
     * @param message сообщение об ошибке
     * @return ResponseEntity с объектом ErrorResponse
     */
    private ResponseEntity<ErrorResponse> buildErrorResponse(HttpStatus status, String message) {
        ErrorResponse response = new ErrorResponse(
                status.value(),
                status.getReasonPhrase(),
                message,
                Instant.now()
        );
        return ResponseEntity.status(status).body(response);
    }

    /**
     * Вспомогательный DTO для структурированного ответа об ошибке.
     * <p>
     * Содержит код, описание, сообщение и время возникновения ошибки.
     * Может использоваться как шаблон для расширения (например, добавить traceId).
     */
    public static class ErrorResponse {
        private final int code;
        private final String reason;
        private final String message;
        private final Instant timestamp;

        public ErrorResponse(int code, String reason, String message, Instant timestamp) {
            this.code = code;
            this.reason = reason;
            this.message = message;
            this.timestamp = timestamp;
        }

        public int getCode() { return code; }
        public String getReason() { return reason; }
        public String getMessage() { return message; }
        public Instant getTimestamp() { return timestamp; }
    }
}
