package org.example.ads.config;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class GenerateHash {
    public static void main(String[] args) {
        PasswordEncoder encoder = new BCryptPasswordEncoder();
        String rawPassword = "123456"; // пароль, который будешь использовать для входа
        String encodedPassword = encoder.encode(rawPassword);
        System.out.println("Хеш для пароля '" + rawPassword + "':");
        System.out.println(encodedPassword);
    }
}
