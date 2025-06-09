package com.example.medicalhomevisit.service;

import com.example.medicalhomevisit.dtos.*;
import com.example.medicalhomevisit.models.entities.*;
import com.example.medicalhomevisit.models.enums.Gender;
import com.example.medicalhomevisit.models.enums.UserRole;
import com.example.medicalhomevisit.repositories.*;
import com.example.medicalhomevisit.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;

@Service
@Transactional
public class AuthService {

    private UserRepository userRepository;
    private RoleRepository roleRepository;
    private MedicalPersonRepository medicalPersonRepository;
    private PatientRepository patientRepository;
    private PasswordEncoder passwordEncoder;
    private AuthenticationManager authenticationManager;
    private JwtTokenProvider tokenProvider;

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
                .orElseThrow(() -> new RuntimeException("Пользователь не найден после успешной аутентификации"));

        return new LoginResponse(jwt, convertToUserDto(user));
    }

    public LoginResponse signUp(PatientSelfRegisterRequest request) {
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
        user.setActive(true);

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

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        String jwt = tokenProvider.generateToken(authentication);

        return new LoginResponse(jwt, convertToUserDto(savedUser));
    }

    public void signOut() {
        SecurityContextHolder.clearContext();
    }

    private UserDto convertToUserDto(UserEntity user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setDisplayName(user.getFullName());

        UserRole roleEnum = null;
        if (user.getRole() != null && user.getRole().getName() != null) {
            dto.setRole(user.getRole().getName().name());
            roleEnum = user.getRole().getName();
        } else {
            dto.setRole(UserRole.PATIENT.name());
            roleEnum = UserRole.PATIENT;
        }

        if (roleEnum == UserRole.MEDICAL_STAFF) {
            medicalPersonRepository.findByUser(user).ifPresent(medicalPerson -> {
                dto.setMedicalPersonId(medicalPerson.getId());
            });
        }
        return dto;
    }

    @Autowired
    public void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Autowired
    public void setRoleRepository(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Autowired
    public void setMedicalPersonRepository(MedicalPersonRepository medicalPersonRepository) {
        this.medicalPersonRepository = medicalPersonRepository;
    }

    @Autowired
    public void setPatientRepository(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    @Autowired
    public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Autowired
    public void setAuthenticationManager(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    @Autowired
    public void setTokenProvider(JwtTokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }
}
