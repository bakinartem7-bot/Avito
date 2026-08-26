package org.example.ads.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.example.ads.security.Role;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column
    private String displayName;

    @Column
    private String phone;

    @Column
    private String city;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(50) default 'USER'")
    private Role role = Role.USER;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    @Column
    private Instant updatedAt;

    @PreUpdate
    private void preUpdate() {
        this.updatedAt = Instant.now();
    }
}
