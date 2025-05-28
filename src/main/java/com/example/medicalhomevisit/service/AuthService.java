package com.example.medicalhomevisit.service;

import com.example.medicalhomevisit.dtos.*; // Импортируем новые DTO
import com.example.medicalhomevisit.models.entities.*;
import com.example.medicalhomevisit.models.enums.Gender;
import com.example.medicalhomevisit.models.enums.UserRole;
import com.example.medicalhomevisit.repositories.*;
import com.example.medicalhomevisit.JwtTokenProvider; // Убедитесь, что этот класс существует и настроен
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList; // Для пустых списков
import java.util.Date;

@Service
@Transactional
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PatientRepository patientRepository; // Убедитесь, что этот репозиторий существует

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager; // Убедитесь, что этот бин сконфигурирован

    @Autowired
    private JwtTokenProvider tokenProvider; // Убедитесь, что этот класс существует и настроен

    public LoginResponse signIn(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);
        UserEntity user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден после успешной аутентификации")); // Это не должно происходить

        return new LoginResponse(jwt, convertToUserDto(user));
    }

    public UserDto signUp(PatientSelfRegisterRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("Пароли не совпадают");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email уже используется");
        }

        UserEntity user = new UserEntity();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setActive(true); // Активируем пользователя сразу

        Role patientRole = roleRepository.findByName(UserRole.PATIENT)
                .orElseGet(() -> {
                    Role newRole = new Role();
                    newRole.setName(UserRole.PATIENT);
                    return roleRepository.save(newRole);
                });
        user.setRole(patientRole);

        UserEntity savedUser = userRepository.save(user);


        Patient patient = new Patient();
        patient.setUser(savedUser);

        patient.setDateOfBirth(null);
        patient.setGender(Gender.UNKNOWN);
        patient.setAddress("Адрес не указан");
        patient.setPhoneNumber("Телефон не указан");
        patient.setPolicyNumber("");
        patient.setAllergies(new ArrayList<>());
        patient.setChronicConditions(new ArrayList<>());

        patientRepository.save(patient);

        return convertToUserDto(savedUser);
    }

    public void signOut() {
        // Для JWT обычно достаточно, чтобы клиент удалил токен.
        // Если нужна серверная логика (например, черный список токенов), она реализуется здесь.
        SecurityContextHolder.clearContext();
    }

    public UserDto getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !(authentication.getPrincipal() instanceof String)) {
            String email = ((org.springframework.security.core.userdetails.User) authentication.getPrincipal()).getUsername();
            return userRepository.findByEmail(email)
                    .map(this::convertToUserDto)
                    .orElse(null);
        }
        return null;
    }

    private UserDto convertToUserDto(UserEntity user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setDisplayName(user.getFullName());
        if (user.getRole() != null && user.getRole().getName() != null) {
            dto.setRole(user.getRole().getName().name());
        } else {
            dto.setRole(UserRole.PATIENT.name());
        }
        dto.setEmailVerified(user.isActive());
        return dto;
    }
}
