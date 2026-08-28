package org.example.ads.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false, length = 50)
    private String username;

    @Column(nullable = false)
    private String passwordHash;

    @Column(unique = true, nullable = false, length = 255)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", length = 50, columnDefinition = "VARCHAR(50) DEFAULT 'USER'")
    private Role role;

    @Column(name = "registered_at", updatable = false)
    private Instant registeredAt;

    @UpdateTimestamp
    private Instant updatedAt;

    @Column(length = 100)
    private String city;

    @Column(length = 20)
    private String phone;

    @OneToMany(mappedBy = "author", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Ad> ads = new ArrayList<>();

    @OneToMany(mappedBy = "author", fetch = FetchType.LAZY)
    private List<Comment> comments = new ArrayList<>();

    @PrePersist
    protected void onPrePersist() {
        if (this.registeredAt == null) {
            this.registeredAt = Instant.now();
        }
        // Если роль не задана — ставим USER (из enum)
        if (this.role == null) {
            this.role = Role.USER;
        }
    }

    public void setDisplayName(String displayName) {
        this.username = displayName;
    }

    // Этот метод можно удалить или оставить пустым — он не нужен
    public void setPassword(String s) {
    }
}
