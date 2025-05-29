package com.example.medicalhomevisit;

import org.modelmapper.ModelMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class MedicalHomeVisitApplication {

    public static void main(String[] args) {
        SpringApplication.run(MedicalHomeVisitApplication.class, args);
    }

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();
        // Здесь можно добавить кастомные конфигурации маппинга, если нужно
        // modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT); // Например, для строгого соответствия имен
        return modelMapper;
    }
}
