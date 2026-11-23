package com.emergency.storage;

import com.emergency.enums.UserRole;
import com.emergency.model.User;
import com.emergency.util.PasswordUtil;

import java.util.ArrayList;

public class UserStorage {

    private ArrayList<User> users;
    private int nextUserId;

    public UserStorage() {
        users = new ArrayList<>();
        nextUserId = 1;
        seedDefaultUsers();
    }

    public void seedDefaultUsers() {
        if (!users.isEmpty()) return;

        String adminSalt = PasswordUtil.generateSalt();
        String adminHash = PasswordUtil.hashPassword("Admin123", adminSalt);
        users.add(new User(generateUserId(), "ADMIN001", "System Admin", "admin@emergency.com",
                "9876543210", adminHash, adminSalt, UserRole.ADMIN));

        String userSalt = PasswordUtil.generateSalt();
        String userHash = PasswordUtil.hashPassword("User123", userSalt);
        users.add(new User(generateUserId(), "USER001", "Citizen User", "user@emergency.com",
                "9123456780", userHash, userSalt, UserRole.USER));
    }

    public int generateUserId() {
        return nextUserId++;
    }

    public void addUser(User user) {
        if (user != null) users.add(user);
    }

    public User findUserById(int userId) {
        for (User user : users) {
            if (user.getUserId() == userId) return user;
        }
        return null;
    }

    public User findUserByEmail(String email) {
        if (email == null) return null;
        String query = email.trim();
        for (User user : users) {
            if (user.getEmail().equalsIgnoreCase(query)) return user;
        }
        return null;
    }

    public User findUserByIdentifier(String identifier) {
        if (identifier == null) return null;
        String query = identifier.trim();
        for (User user : users) {
            if (user.getEmail().equalsIgnoreCase(query)) return user;
            if (user.getUserCode() != null && user.getUserCode().equalsIgnoreCase(query)) return user;
            if (String.valueOf(user.getUserId()).equalsIgnoreCase(query)) return user;
        }
        return null;
    }

    public ArrayList<User> getAllUsers() {
        return new ArrayList<>(users);
    }

    public boolean removeUser(int userId) {
        User user = findUserById(userId);
        if (user != null) {
            users.remove(user);
            return true;
        }
        return false;
    }
}
