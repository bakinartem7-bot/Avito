package org.example.ads.dto;

import lombok.Data;
import java.time.Instant;
import java.util.UUID;

@Data
public class UserDto {
    private UUID id;
    private String email;
    private Instant createdAt;
}
