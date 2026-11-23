package com.emergency.controller;

import com.emergency.enums.UserRole;
import com.emergency.model.User;
import com.emergency.service.UserService;
import com.emergency.views.RegisterView;

public class RegisterController {

    private RegisterView registerView;
    private UserService userService;
    private Runnable backAction;

    public RegisterController(RegisterView registerView, UserService userService, Runnable backAction) {
        this.registerView = registerView;
        this.userService = userService;
        this.backAction = backAction;

        registerView.getRegisterButton().setOnAction(e -> registerUser());
        registerView.getLoginButton().setOnAction(e -> backAction.run());
    }

    private void registerUser() {
        try {
            String name = registerView.getNameField().getText();
            String email = registerView.getEmailField().getText();
            String phone = registerView.getPhoneField().getText();
            String password = registerView.getPasswordField().getText();
            String confirmPassword = registerView.getConfirmPasswordField().getText();
            String roleStr = registerView.getRoleBox().getValue();

            if (name == null || name.trim().isEmpty() ||
                email == null || email.trim().isEmpty() ||
                phone == null || phone.trim().isEmpty() ||
                password == null || password.isEmpty() ||
                confirmPassword == null || confirmPassword.isEmpty()) {
                registerView.getMessageLabel().setText("Please fill in all the required fields.");
                registerView.getMessageLabel().setStyle("-fx-text-fill: #DC2626;");
                return;
            }

            if (!password.equals(confirmPassword)) {
                registerView.getMessageLabel().setText("Passwords do not match. Please verify.");
                registerView.getMessageLabel().setStyle("-fx-text-fill: #DC2626;");
                return;
            }

            UserRole role = (roleStr != null && roleStr.equalsIgnoreCase("Admin")) ? UserRole.ADMIN : UserRole.USER;
            User user = userService.registerUser(name.trim(), email.trim(), phone.trim(), password, role);

            registerView.getMessageLabel().setText("Registered successfully! Your User ID is " + user.getUserCode() + ". Click below to sign in.");
            registerView.getMessageLabel().setStyle("-fx-text-fill: #16A34A; -fx-font-weight: bold;");

            registerView.getNameField().clear();
            registerView.getEmailField().clear();
            registerView.getPhoneField().clear();
            registerView.getPasswordField().clear();
            registerView.getConfirmPasswordField().clear();

        } catch (IllegalArgumentException e) {
            registerView.getMessageLabel().setText(e.getMessage());
            registerView.getMessageLabel().setStyle("-fx-text-fill: #DC2626;");
        }
    }
}
