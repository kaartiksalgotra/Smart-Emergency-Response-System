package com.emergency.model;

import com.emergency.enums.UserRole;
import java.time.LocalDateTime;

public class User {

    private int userId;
    private String userCode;
    private String name;
    private String email;
    private String phone;
    private String passwordHash;
    private String passwordSalt;
    private UserRole role;
    private LocalDateTime createdAt;

    public User(int userId, String userCode, String name, String email, String phone,
                String passwordHash, String passwordSalt, UserRole role) {
        this.userId = userId;
        this.userCode = userCode;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.passwordHash = passwordHash;
        this.passwordSalt = passwordSalt;
        this.role = role;
        this.createdAt = LocalDateTime.now();
    }

    public User(int userId, String name, String email, String phone,
                String passwordHash, String passwordSalt, UserRole role) {
        this(userId, (role == UserRole.ADMIN ? "ADMIN" : "USER") + String.format("%03d", userId),
             name, email, phone, passwordHash, passwordSalt, role);
    }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getUserCode() { return userCode; }
    public void setUserCode(String userCode) { this.userCode = userCode; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getPasswordSalt() { return passwordSalt; }
    public void setPasswordSalt(String passwordSalt) { this.passwordSalt = passwordSalt; }

    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }

    public LocalDateTime getCreatedAt() { return createdAt; }
}
