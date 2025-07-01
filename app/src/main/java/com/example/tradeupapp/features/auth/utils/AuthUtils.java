package com.example.tradeupapp.features.auth.utils;

import android.util.Patterns;

public class AuthUtils {

    /**
     * Validates email format using Android's built-in Patterns class
     * @param email The email string to validate
     * @return true if email format is valid, false otherwise
     */
    public static boolean isValidEmail(String email) {
        return email != null && !email.trim().isEmpty() && Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches();
    }

    /**
     * Validates password strength
     * @param password The password string to validate
     * @return true if password meets requirements, false otherwise
     */
    public static boolean isValidPassword(String password) {
        return password != null && password.length() >= 6;
    }

    /**
     * Validates password strength with detailed requirements
     * @param password The password string to validate
     * @return true if password meets all requirements, false otherwise
     */
    public static boolean isStrongPassword(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }

        boolean hasUpperCase = false;
        boolean hasLowerCase = false;
        boolean hasDigit = false;
        boolean hasSpecialChar = false;

        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) {
                hasUpperCase = true;
            } else if (Character.isLowerCase(c)) {
                hasLowerCase = true;
            } else if (Character.isDigit(c)) {
                hasDigit = true;
            } else if (!Character.isLetterOrDigit(c)) {
                hasSpecialChar = true;
            }
        }

        return hasUpperCase && hasLowerCase && hasDigit && hasSpecialChar;
    }

    /**
     * Gets password strength message
     * @param password The password to check
     * @return String message describing password requirements
     */
    public static String getPasswordStrengthMessage(String password) {
        if (password == null || password.isEmpty()) {
            return "Mật khẩu không được để trống";
        }

        if (password.length() < 6) {
            return "Mật khẩu phải có ít nhất 6 ký tự";
        }

        if (password.length() < 8) {
            return "Mật khẩu nên có ít nhất 8 ký tự để bảo mật tốt hơn";
        }

        return "Mật khẩu hợp lệ";
    }

    /**
     * Formats Firebase authentication error messages to Vietnamese
     * @param errorCode The Firebase error code
     * @return Localized error message in Vietnamese
     */
    public static String getFirebaseErrorMessage(String errorCode) {
        if (errorCode == null) {
            return "Có lỗi xảy ra. Vui lòng thử lại";
        }

        switch (errorCode) {
            case "ERROR_INVALID_EMAIL":
                return "Địa chỉ email không hợp lệ";
            case "ERROR_WRONG_PASSWORD":
                return "Mật khẩu không chính xác";
            case "ERROR_USER_NOT_FOUND":
                return "Không tìm thấy tài khoản với email này";
            case "ERROR_USER_DISABLED":
                return "Tài khoản đã bị vô hiệu hóa";
            case "ERROR_TOO_MANY_REQUESTS":
                return "Quá nhiều yêu cầu. Vui lòng thử lại sau";
            case "ERROR_EMAIL_ALREADY_IN_USE":
                return "Email này đã được sử dụng cho tài khoản khác";
            case "ERROR_WEAK_PASSWORD":
                return "Mật khẩu quá yếu. Vui lòng chọn mật khẩu mạnh hơn";
            case "ERROR_NETWORK_REQUEST_FAILED":
                return "Lỗi kết nối mạng. Vui lòng kiểm tra internet";
            default:
                return "Có lỗi xảy ra. Vui lòng thử lại";
        }
    }
}
