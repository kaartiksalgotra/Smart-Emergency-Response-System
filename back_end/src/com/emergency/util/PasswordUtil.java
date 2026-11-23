
package com.emergency.util;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Base64;

public class PasswordUtil {

    private static final int ITERATIONS = 65536;
    private static final int KEY_LENGTH = 256;

    public static String generateSalt() {

        byte[] salt = new byte[16];

        SecureRandom random = new SecureRandom();
        random.nextBytes(salt);

        return Base64.getEncoder().encodeToString(salt);
    }

    public static String hashPassword(String password, String salt) {

        try {

            byte[] saltBytes = Base64.getDecoder().decode(salt);

            KeySpec spec = new PBEKeySpec(
                    password.toCharArray(),
                    saltBytes,
                    ITERATIONS,
                    KEY_LENGTH);

            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");

            byte[] hash = factory.generateSecret(spec).getEncoded();

            return Base64.getEncoder().encodeToString(hash);

        } catch (Exception e) {
            throw new RuntimeException("Error while hashing password", e);
        }
    }

    public static boolean verifyPassword(String password, String salt, String storedHash) {

        String newHash = hashPassword(password, salt);

        byte[] newHashBytes = Base64.getDecoder().decode(newHash);
        byte[] storedHashBytes = Base64.getDecoder().decode(storedHash);

        return java.security.MessageDigest.isEqual(
                newHashBytes,
                storedHashBytes);
    }
}
