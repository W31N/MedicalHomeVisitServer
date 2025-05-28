//package com.example.medicalhomevisit.controllers;
//
//import com.example.medicalhomevisit.dtos.VisitDto;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//import java.util.UUID;
//
//@RestController
//@RequestMapping("/api/visits")
//public class VisitController {
//
//    @GetMapping("/today")
//    public ResponseEntity<List<VisitDto>> getVisitsForToday() {
//        // Визиты на сегодня
//    }
//
//    @GetMapping("/staff/{staffId}")
//    public ResponseEntity<List<VisitDto>> getVisitsForStaff(@PathVariable UUID staffId) {
//        // Визиты для конкретного медработника
//    }
//
//    @PutMapping("/{visitId}/status")
//    public ResponseEntity<?> updateVisitStatus(@PathVariable UUID visitId, @RequestBody UpdateStatusRequest request) {
//        // Обновление статуса визита
//    }
//}