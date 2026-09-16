package com.sliit.vrs.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

// Single login table for every actor in the system (customer or staff).
// Kept simple on purpose: one table + a "role" column instead of many
// separate login tables, so it is easy to explain to the instructor.
// v2: added address + driving licence fields for a proper customer
// profile page, matching the proposal's "Customer Profile ... Verification"
// scope item.
@Entity
@Table(name = "users")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @NotBlank(message = "Full name is required")
    @Column(nullable = false)
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Enter a valid email address")
    @Column(nullable = false, unique = true)
    private String email;

    // NOTE: In a production system this must be BCrypt-hashed (the proposal
    // requires this under Security/NFR 4.1). We use PasswordEncoder in
    // AuthService - see AuthService.java.
    @Column(nullable = false)
    private String password;

    private String phoneNumber;
    private String address;
    private String drivingLicenceNo;
    private String profileImageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;
}
