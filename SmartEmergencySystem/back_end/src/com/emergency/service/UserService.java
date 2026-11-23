package com.emergency.service;

import com.emergency.enums.UserRole;
import com.emergency.model.User;
import com.emergency.storage.UserStorage;
import com.emergency.util.InputValidator;
import com.emergency.util.PasswordUtil;

import java.util.ArrayList;

public class UserService {

    private UserStorage userStorage;

    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public User registerUser(String name, String email, String phone, String password, UserRole role) {
        if (!InputValidator.isValidName(name)) {
            throw new IllegalArgumentException("Invalid name. Please enter a valid full name.");
        }
        if (!InputValidator.isValidEmail(email)) {
            throw new IllegalArgumentException("Invalid email address format.");
        }
        if (!InputValidator.isValidPhone(phone)) {
            throw new IllegalArgumentException("Phone number must contain exactly 10 digits.");
        }
        if (!InputValidator.isValidPassword(password)) {
            throw new IllegalArgumentException("Password must be at least 6 characters long.");
        }
        if (userStorage.findUserByEmail(email) != null) {
            throw new IllegalArgumentException("An account with this email is already registered.");
        }
        if (role == null) {
            role = UserRole.USER;
        }

        String salt = PasswordUtil.generateSalt();
        String passwordHash = PasswordUtil.hashPassword(password, salt);
        int userId = userStorage.generateUserId();
        String prefix = (role == UserRole.ADMIN) ? "ADMIN" : "USER";
        String userCode = prefix + String.format("%03d", userId);

        User user = new User(userId, userCode, name, email, phone, passwordHash, salt, role);
        userStorage.addUser(user);
        return user;
    }

    public User registerUser(String name, String email, String phone, String password) {
        return registerUser(name, email, phone, password, UserRole.USER);
    }

    public User getUserById(int userId) {
        return userStorage.findUserById(userId);
    }

    public User getUserByEmail(String email) {
        return userStorage.findUserByEmail(email);
    }

    public User getUserByIdentifier(String identifier) {
        return userStorage.findUserByIdentifier(identifier);
    }

    public ArrayList<User> getAllUsers() {
        return userStorage.getAllUsers();
    }

    public boolean removeUser(int userId) {
        return userStorage.removeUser(userId);
    }
}
