package com.tenniscourts.reservations;

import com.tenniscourts.schedules.ScheduleDTO;
import lombok.AllArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("reservations-schedules")
public class ReservationScheduleController {

    private final ReservationService reservationService;

    @GetMapping("/available")
    public ResponseEntity<List<ScheduleDTO>> findReservationsByDate(
            @RequestParam("startDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(pattern = "yyyy-MM-dd")  LocalDate endDate) {
        return ResponseEntity.ok(reservationService.findAvailableSchedulesByDate(LocalDateTime.of(startDate, LocalTime.of(0, 0)), LocalDateTime.of(endDate, LocalTime.of(23, 59))));
    }

}
