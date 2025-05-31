package com.example.medicalhomevisit.controllers;

import com.example.medicalhomevisit.dtos.LoginRequest;
import com.example.medicalhomevisit.dtos.LoginResponse;
import com.example.medicalhomevisit.dtos.PatientSelfRegisterRequest;
import com.example.medicalhomevisit.dtos.UserDto;
import com.example.medicalhomevisit.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) { // Добавлена валидация
        try {
            LoginResponse loginResponse = authService.signIn(request);
            return ResponseEntity.ok(loginResponse);
        } catch (RuntimeException e) { // Ловим более общие ошибки аутентификации
            // В идеале здесь должны быть более специфичные исключения и обработка
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody PatientSelfRegisterRequest request) { // Используем PatientSelfRegisterRequest и валидацию
        try {
            LoginResponse loginResponse = authService.signUp(request);
            // В случае успеха можно вернуть созданного пользователя или просто статус 201 Created
            // Возврат UserDto может быть полезен клиенту для немедленного использования данных
            return ResponseEntity.status(HttpStatus.CREATED).body(loginResponse);
        } catch (RuntimeException e) {
            // Например, "Пароли не совпадают" или "Email уже используется"
            // Эти ошибки лучше обрабатывать с помощью @RestControllerAdvice и кастомных исключений
            // для возврата более структурированных ошибок, но для начала и так сойдет.
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        // Серверная часть JWT logout обычно сложнее, чем просто очистка контекста,
        // так как токены без состояния. Возможные стратегии: черный список токенов,
        // короткое время жизни токенов + refresh токены.
        // Для простоты в ВКР часто ограничиваются тем, что клиент просто удаляет токен.
        authService.signOut(); // Очищает SecurityContextHolder на сервере
        return ResponseEntity.ok("Выход из системы выполнен успешно");
    }
}
