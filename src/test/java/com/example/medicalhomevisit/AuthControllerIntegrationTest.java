package com.example.medicalhomevisit;

import com.example.medicalhomevisit.dtos.LoginRequest;
import com.example.medicalhomevisit.dtos.PatientSelfRegisterRequest;
import com.example.medicalhomevisit.repositories.UserRepository;
import com.example.medicalhomevisit.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
public class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    private final String TEST_EMAIL = "testuser@example.com";
    private final String TEST_PASSWORD = "password123";

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        PatientSelfRegisterRequest registerRequest = new PatientSelfRegisterRequest();
        registerRequest.setEmail(TEST_EMAIL);
        registerRequest.setFullName("Test User");
        registerRequest.setPassword(TEST_PASSWORD);
        registerRequest.setConfirmPassword(TEST_PASSWORD);

        authService.signUp(registerRequest);
    }

    @Test
    @DisplayName("Вход с корректными данными должен вернуть статус 200 OK и JWT токен")
    void login_withValidCredentials_shouldReturnOkAndToken() throws Exception {
        // Arrange
        LoginRequest loginRequest = new LoginRequest(TEST_EMAIL, TEST_PASSWORD);

        // Act & Assert (Действие и Проверка)
        mockMvc.perform(post("/api/auth/login") // Отправляем POST-запрос на эндпоинт
                        .contentType(MediaType.APPLICATION_JSON) // Указываем тип контента
                        .content(objectMapper.writeValueAsString(loginRequest))) // Превращаем объект в JSON
                // Проверяем, что HTTP-статус ответа 200 OK
                .andExpect(status().isOk())
                // Проверяем, что в JSON-ответе есть поле "token"
                .andExpect(jsonPath("$.token").exists())
                // Проверяем, что в JSON-ответе есть вложенный объект "user" с правильным email
                .andExpect(jsonPath("$.user.email").value(TEST_EMAIL));
    }

    @Test
    @DisplayName("Вход с неверным паролем должен вернуть статус 401 Unauthorized")
    void login_withInvalidPassword_shouldReturnUnauthorized() throws Exception {
        // Arrange
        LoginRequest loginRequest = new LoginRequest(TEST_EMAIL, "wrong-password");

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                // Проверяем, что HTTP-статус ответа 401 Unauthorized
                .andExpect(status().isUnauthorized());
    }
}