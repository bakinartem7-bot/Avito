package org.example.ads.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserProfileUpdateDto {
    private String displayName;
    private String phone;
    private String city; //обновление профиля
}
