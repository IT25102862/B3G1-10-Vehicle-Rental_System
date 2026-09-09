package com.sliit.vrs.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

// Single login table for every actor in the system (customer or staff).
// Kept simple on purpose: one table + a "role" column instead of many
// separate login tables, so it is easy to explain to the instructor.
@Entity
@Table(name = "users")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false, unique = true)
    private String email;

    // NOTE: In a production system this must be BCrypt-hashed (the proposal
    // requires this under Security/NFR 4.1). We use PasswordEncoder in
    // AuthService - see AuthService.java.
    @Column(nullable = false)
    private String password;

    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;
}
