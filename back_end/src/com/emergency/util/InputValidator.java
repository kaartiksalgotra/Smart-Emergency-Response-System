
package com.emergency.util;

public class InputValidator {

    public static boolean isValidName(String name) {

        if (name == null || name.trim().isEmpty()) {
            return false;
        }

        return true;
    }

    public static boolean isValidEmail(String email) {

        if (email == null || email.trim().isEmpty()) {
            return false;
        }

        String emailPattern = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";

        return email.matches(emailPattern);
    }

    public static boolean isValidPhone(String phone) {

        if (phone == null) {
            return false;
        }

        return phone.matches("[0-9]{10}");
    }

    public static boolean isValidPassword(String password) {
        if (password == null || password.trim().length() < 6) {
            return false;
        }
        return true;
    }

    public static boolean isValidDescription(String description) {

        if (description == null || description.trim().isEmpty()) {
            return false;
        }

        return true;
    }

    public static boolean isValidAffectedPeople(int affectedPeople) {
        return affectedPeople > 0;
    }
}
