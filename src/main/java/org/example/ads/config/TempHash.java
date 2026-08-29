package org.example.ads.config; // или любой другой пакет

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class TempHash {
    public static void main(String[] args) {
        PasswordEncoder encoder = new BCryptPasswordEncoder();
        // Вставь сюда свой будущий пароль
        String rawPassword = "123456";
        String encodedPassword = encoder.encode(rawPassword);
        System.out.println("Хеш для пароля '" + rawPassword + "': " + encodedPassword);
    }
}
