package com.emergency.service;

import com.emergency.enums.UserRole;
import com.emergency.model.User;
import com.emergency.storage.UserStorage;
import com.emergency.util.PasswordUtil;

public class AuthenticationService {

    private UserStorage userStorage;

    public AuthenticationService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public User login(String identifier, String password) {
        if (identifier == null || identifier.trim().isEmpty() || password == null || password.isEmpty()) {
            return null;
        }
        User user = userStorage.findUserByIdentifier(identifier);
        if (user == null) return null;

        if (PasswordUtil.verifyPassword(password, user.getPasswordSalt(), user.getPasswordHash())) {
            return user;
        }
        return null;
    }

    public User login(String identifier, String password, UserRole selectedRole) {
        User user = login(identifier, password);
        if (user != null && user.getRole() == selectedRole) {
            return user;
        }
        return null;
    }

    public String validateLogin(String identifier, String password, UserRole selectedRole) {
        if (identifier == null || identifier.trim().isEmpty()) {
            return "Please enter your User ID or Email.";
        }
        if (password == null || password.isEmpty()) {
            return "Please enter your password.";
        }
        if (selectedRole == null) {
            return "Please select your login role (Citizen or Admin).";
        }

        User user = userStorage.findUserByIdentifier(identifier);
        if (user == null) {
            return "Invalid User ID or Email. Account not found.";
        }

        boolean passOk = PasswordUtil.verifyPassword(password, user.getPasswordSalt(), user.getPasswordHash());
        if (!passOk) {
            return "Invalid password. Please try again.";
        }

        if (user.getRole() != selectedRole) {
            String actRole = (user.getRole() == UserRole.ADMIN) ? "Admin" : "Citizen/User";
            String selRole = (selectedRole == UserRole.ADMIN) ? "Admin" : "Citizen/User";
            return "Role mismatch: This account is registered as " + actRole + ", not " + selRole + ".";
        }

        return null; // All valid
    }

    public User getUserByEmail(String email) {
        return userStorage.findUserByEmail(email);
    }

    public User getUserByIdentifier(String identifier) {
        return userStorage.findUserByIdentifier(identifier);
    }

    public boolean isAdmin(User user) {
        return user != null && user.getRole() == UserRole.ADMIN;
    }

    public boolean isNormalUser(User user) {
        return user != null && user.getRole() == UserRole.USER;
    }
}
