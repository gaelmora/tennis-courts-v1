package com.tenniscourts.reservations;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ReservationRepository extends PagingAndSortingRepository<Reservation, Long> {

    List<Reservation> findBySchedule_Id(Long scheduleId);

    List<Reservation> findByReservationStatusAndSchedule_StartDateTimeGreaterThanEqualAndSchedule_EndDateTimeLessThanEqual(ReservationStatus reservationStatus, LocalDateTime startDateTime, LocalDateTime endDateTime);

    List<Reservation> findByReservationStatusAndSchedule_StartDateTimeGreaterThanEqualAndSchedule_EndDateTimeLessThanEqualAndSchedule_TennisCourtId(ReservationStatus reservationStatus, LocalDateTime startDateTime, LocalDateTime endDateTime, Long tennisCourtId);

    Page<Reservation> findByDateCreateBetweenOrderByDateCreate(LocalDateTime startDate, LocalDateTime endDate, Pageable paging);

//    List<Reservation> findByStartDateTimeGreaterThanEqualAndEndDateTimeLessThanEqualAndTennisCourt(LocalDateTime startDateTime, LocalDateTime endDateTime, TennisCourt tennisCourt);
}
