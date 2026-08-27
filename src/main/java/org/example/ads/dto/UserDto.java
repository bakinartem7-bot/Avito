package org.example.ads.dto;

import org.example.ads.entity.User;
import java.time.Instant;
import java.util.UUID;

public class UserDto {
    private final UUID id;
    private final String username;
    private final String email;
    private final String role;
    private final Instant registeredAt;
    private final String city;
    private final String phone;

    public UserDto(User u) {
        this.id = u.getId();
        this.username = u.getUsername();
        this.email = u.getEmail();
        this.role = u.getRole() != null ? u.getRole().name() : null;

        this.registeredAt = u.getRegisteredAt();

        this.city = u.getCity();
        this.phone = u.getPhone();
    }
    public UUID getId() { return id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
    public Instant getRegisteredAt() { return registeredAt; }
    public String getCity() { return city; }
    public String getPhone() { return phone; }
}
