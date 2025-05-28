package com.example.medicalhomevisit;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtTokenProvider {
    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class); // Логгер

    @Value("${jwt.secret}")
    private String jwtSecretString; // Переименовал для ясности, что это строка из properties

    @Value("${jwt.expiration}")
    private int jwtExpirationMs; // Предполагаем, что это в миллисекундах

    private SecretKey key; // <-- Поле для хранения объекта ключа

    @PostConstruct // Этот метод будет вызван после создания бина и внедрения зависимостей
    public void init() {
        // Декодируем строку секрета из Base64URL и создаем объект SecretKey
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64URL.decode(jwtSecretString));
    }

    public String generateToken(Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .setSubject(userPrincipal.getUsername())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                // Используем объект Key и явно указываем алгоритм, хотя для HMAC он может быть выведен из ключа
                .signWith(key, SignatureAlgorithm.HS512) // ИЛИ просто .signWith(key)
                .compact();
    }

    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parserBuilder() // Используем parserBuilder для современного API
                .setSigningKey(key)       // Используем объект Key
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }

    public boolean validateToken(String authToken) {
        try {
            Jwts.parserBuilder()        // Используем parserBuilder
                    .setSigningKey(key)   // Используем объект Key
                    .build()
                    .parseClaimsJws(authToken);
            return true;
        } catch (SignatureException ex) {
            logger.error("Invalid JWT signature: {}", ex.getMessage());
        } catch (MalformedJwtException ex) {
            logger.error("Invalid JWT token: {}", ex.getMessage());
        } catch (ExpiredJwtException ex) {
            logger.error("Expired JWT token: {}", ex.getMessage());
        } catch (UnsupportedJwtException ex) {
            logger.error("Unsupported JWT token: {}", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            logger.error("JWT claims string is empty or key is invalid: {}", ex.getMessage());
        }
        return false;
    }
}
