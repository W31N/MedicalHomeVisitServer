package com.example.medicalhomevisit.service;

import com.example.medicalhomevisit.models.entities.UserEntity; // Используем вашу JPA-сущность
import com.example.medicalhomevisit.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Правильный импорт

import java.util.HashSet;
import java.util.Set;

@Service
public class UserDetailsServiceImpl implements UserDetailsService { // Реализуем интерфейс

    private final UserRepository userRepository;

    @Autowired
    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true) // Используем аннотацию Spring
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UserEntity userEntity = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь с email: " + email + " не найден"));

        Set<GrantedAuthority> authorities = new HashSet<>();
        if (userEntity.getRole() != null && userEntity.getRole().getName() != null) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + userEntity.getRole().getName().name()));
        } else {
            throw new IllegalStateException("Роль для пользователя " + email + " не определена.");
        }

        // Используем импортированный SpringSecurityUser и метод getPasswordHash()
        return new User(userEntity.getEmail(), userEntity.getPassword(), userEntity.isActive(),
                true, true, true, authorities);
    }
}