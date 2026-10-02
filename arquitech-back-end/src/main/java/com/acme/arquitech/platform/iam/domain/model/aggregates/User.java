package com.acme.arquitech.platform.iam.domain.model.aggregates;

import com.acme.arquitech.platform.iam.domain.model.valueobjects.Role;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Entity
@Table(name = "users", uniqueConstraints = @UniqueConstraint(name = "uk_users_email", columnNames = "email"))
@Getter
@NoArgsConstructor
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String email;
    @JsonIgnore
    private String password;
    @Enumerated(EnumType.STRING)
    private Role role;
    private String profilePicture;
    private String phone;
    @Column(updatable = false)
    private OffsetDateTime createdAt;

    public User(String name, String email, String password, Role role, String profilePicture, String phone) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
        this.profilePicture = profilePicture;
        this.phone = phone;
    }

    @PrePersist
    void onCreate() {
        createdAt = OffsetDateTime.now(ZoneOffset.UTC);
    }

    public void updateProfile(String fullName, String phone) {
        this.name = fullName;
        this.phone = phone;
    }
}
