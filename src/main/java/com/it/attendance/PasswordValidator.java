package com.it.attendance;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PasswordValidator {

    public static boolean isValidPassword(String password) {
        if (password.isEmpty()) {
            return false;  // Empty password is not allowed
        }

        if (password.length() < 8) {
            return false;  // Password too short
        }

        int hasUpperCase = 0;
        int hasLowerCase = 0;
        int hasNumber = 0;
        int hasSpecialChar = 0;
        String regex = "[!@#\\$%\\^&\\*\\(\\)_\\+]";
        // Compile the regex pattern
        Pattern pattern = Pattern.compile(regex);
        // Create a matcher object
        Matcher matcher = pattern.matcher(password);

            while (matcher.find()) {
                hasSpecialChar++;
            }
        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) {
                hasUpperCase++;
            } else if (Character.isLowerCase(c)) {
                hasLowerCase++;
            } else if (Character.isDigit(c)) {
                hasNumber++;

            }
        }

        return hasUpperCase > 0 && hasLowerCase > 0 && hasNumber > 0 && hasSpecialChar > 0;

}
}
