package com.example.medicalhomevisit;

import com.example.medicalhomevisit.models.entities.Role;
import com.example.medicalhomevisit.models.enums.UserRole;
import com.example.medicalhomevisit.repositories.RoleRepository;
import com.example.medicalhomevisit.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserService userService;

    @Override
    public void run(String... args) throws Exception {
        // Создание ролей
        for (UserRole userRole : UserRole.values()) {
            if (!roleRepository.findByName(userRole).isPresent()) {
                Role role = new Role();
                role.setName(userRole);
                roleRepository.save(role);
            }
        }

        // Создание тестового администратора
        if (!userRepository.existsByEmail("admin@medicalhomevisit.com")) {
            RegisterRequest adminRequest = new RegisterRequest();
            adminRequest.setEmail("admin@medicalhomevisit.com");
            adminRequest.setPassword("Admin123");
            adminRequest.setFullName("Администратор Системы");
            adminRequest.setRole(UserRole.ADMIN);
            userService.createUser(adminRequest);
        }
    }
}
