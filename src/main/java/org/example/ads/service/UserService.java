package org.example.ads.service;

import org.example.ads.dto.UserDto;
import org.example.ads.entity.User;
import org.example.ads.exception.NotFoundException;
import org.example.ads.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Сервис для работы с данными пользователей.
 * Предоставляет методы для получения информации о пользователе
 * и преобразования сущностей в DTO.
 */
@Service
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    /**
     * Создаёт экземпляр UserService с внедрённым репозиторием.
     *
     * @param userRepository репозиторий для доступа к данным пользователей
     */
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Получает данные пользователя по его идентификатору.
     * <p>
     * Если пользователь не найден, выбрасывается исключение NotFoundException.
     * </p>
     *
     * @param id идентификатор пользователя
     * @return DTO с информацией о пользователе (email, id, дата создания)
     * @throws NotFoundException если пользователь с заданным id не существует
     */
    public UserDto getUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));
        return toDto(user);
    }

    /**
     * Преобразует сущность User в DTO UserDto.
     * <p>
     * Используется внутри сервиса для формирования ответа.
     * </p>
     *
     * @param user сущность пользователя
     * @return DTO-объект с данными пользователя
     */
    private UserDto toDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setCreatedAt(user.getCreatedAt());
        return dto;
    }
}
