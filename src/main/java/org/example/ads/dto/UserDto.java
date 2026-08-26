package org.example.ads.dto;

import lombok.Data;
import org.example.ads.entity.User;
import org.example.ads.security.Role;
import java.time.Instant;
import java.util.UUID;

@Data
public class UserDto {
    private UUID id;
    private String email;
    private String displayName;
    private String phone;
    private String city;
    private Instant createdAt;
    private Role role;
    private Instant updatedAt;

    public static UserDto fromEntity(User u) {
        UserDto dto = new UserDto();
        dto.setId(u.getId());
        dto.setEmail(u.getEmail());
        dto.setDisplayName(u.getDisplayName());
        dto.setPhone(u.getPhone());
        dto.setCity(u.getCity());
        dto.setCreatedAt(u.getCreatedAt());
        return dto;
    }
}
