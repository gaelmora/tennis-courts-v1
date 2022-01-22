package com.tenniscourts.reservations;

import com.tenniscourts.exceptions.EntityNotFoundException;
import com.tenniscourts.schedules.ScheduleDTO;
import com.tenniscourts.schedules.ScheduleService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;

    private final ReservationMapper reservationMapper;

    private final ScheduleService scheduleService;

    private final static BigDecimal RESERVATION_VALUE = new BigDecimal(20);

    private final static BigDecimal RESERVATION_DEPOSIT = BigDecimal.TEN;

    public ReservationDTO bookReservation(CreateReservationRequestDTO createReservationRequestDTO) {
        ScheduleDTO schedule = scheduleService.findSchedule(createReservationRequestDTO.getScheduleId());

        if (schedule.getStartDateTime().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Can book reservation only future dates.");
        }

        List<Reservation> scheduledReservations = reservationRepository.findByReservationStatusAndSchedule_StartDateTimeGreaterThanEqualAndSchedule_EndDateTimeLessThanEqualAndSchedule_TennisCourtId(
                ReservationStatus.READY_TO_PLAY, schedule.getStartDateTime(), schedule.getEndDateTime(), schedule.getTennisCourt().getId());
        if (!scheduledReservations.isEmpty()) {
            throw new IllegalArgumentException("This schedule is not available.");
        }

        Reservation reservation = reservationMapper.map(createReservationRequestDTO);
        reservation.setValue(RESERVATION_VALUE);
        return reservationMapper.map(reservationRepository.save(reservation));
    }

    List<ReservationDTO> findReservationsByDate(LocalDateTime startDate, LocalDateTime endDate, Pageable paging) {
        return reservationMapper.map(reservationRepository.findByDateCreateBetweenOrderByDateCreate(startDate, endDate, paging).getContent());
    }

    public ReservationDTO findReservation(Long reservationId) {
        return reservationMapper.map(this.findById(reservationId));
    }

    private Reservation findById(Long reservationId) {
        Optional<Reservation> reservation = reservationRepository.findById(reservationId);
        if (!reservation.isPresent()) {
            throw new EntityNotFoundException("Reservation not found.");
        }
        return reservation.get();
    }

    public ReservationDTO cancelReservation(Long reservationId) {
        return reservationMapper.map(this.cancel(this.findById(reservationId)));
    }

    private Reservation cancel(Reservation reservation) {
        this.validateCancellation(reservation);
        BigDecimal refundValue = this.getRefundValue(reservation);
        return this.updateReservation(reservation, refundValue, ReservationStatus.CANCELLED);
    }

    private Reservation updateReservation(Reservation reservation, BigDecimal refundValue, ReservationStatus status) {
        reservation.setReservationStatus(status);
        reservation.setValue(reservation.getValue().subtract(refundValue));
        reservation.setRefundValue(refundValue);

        return reservationRepository.save(reservation);
    }

    private void validateCancellation(Reservation reservation) {
        if (!ReservationStatus.READY_TO_PLAY.equals(reservation.getReservationStatus())) {
            throw new IllegalArgumentException("Cannot cancel/reschedule because it's not in ready to play status.");
        }

        if (reservation.getSchedule().getStartDateTime().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Can cancel/reschedule only future dates.");
        }
    }

    public BigDecimal getRefundValue(Reservation reservation) {
        LocalDateTime current = LocalDateTime.now();
        long hours = ChronoUnit.HOURS.between(current, reservation.getSchedule().getStartDateTime());
        BigDecimal refundValue = BigDecimal.ZERO;

        if (hours >= 24) {
            refundValue = RESERVATION_DEPOSIT;
        } else if (hours >= 0 && hours < 24){
            if (current.isAfter(LocalDateTime.of(current.getYear(), current.getMonth(), current.getDayOfMonth(), 12, 00, 00))
                    && current.isBefore(LocalDateTime.of(current.getYear(), current.getMonth(), current.getDayOfMonth(), 23, 59, 00))) {
                refundValue = refundValue.subtract(RESERVATION_DEPOSIT.multiply(new BigDecimal(.25)));
            }

            if (current.isAfter(LocalDateTime.of(current.getYear(), current.getMonth(), current.getDayOfMonth(), 02, 01, 00))
                    && current.isBefore(LocalDateTime.of(current.getYear(), current.getMonth(), current.getDayOfMonth(), 11, 59, 00))) {
                refundValue = refundValue.subtract(RESERVATION_DEPOSIT.multiply(new BigDecimal(.50)));
            }

            if (current.isAfter(LocalDateTime.of(current.getYear(), current.getMonth(), current.getDayOfMonth(), 00, 01, 00))
                    && current.isBefore(LocalDateTime.of(current.getYear(), current.getMonth(), current.getDayOfMonth(), 02, 00, 00))) {
                refundValue = refundValue.subtract(RESERVATION_DEPOSIT.multiply(new BigDecimal(.75)));
            }
        }

        return refundValue;
    }

    public ReservationDTO rescheduleReservation(Long previousReservationId, Long scheduleId) {
        Reservation previousReservation = this.findById(previousReservationId);

        this.validateCancellation(previousReservation);

        if (scheduleId.equals(previousReservation.getSchedule().getId())) {
            throw new IllegalArgumentException("Cannot reschedule to the same slot.");
        }

        ScheduleDTO schedule = scheduleService.findSchedule(scheduleId);

        List<Reservation> scheduledReservations = reservationRepository.findByReservationStatusAndSchedule_StartDateTimeGreaterThanEqualAndSchedule_EndDateTimeLessThanEqualAndSchedule_TennisCourtId(
                ReservationStatus.READY_TO_PLAY, schedule.getStartDateTime(), schedule.getEndDateTime(), schedule.getTennisCourt().getId());
        if (!scheduledReservations.isEmpty()) {
            throw new IllegalArgumentException("This schedule is not available.");
        }

        BigDecimal refundValue = this.getRefundValue(previousReservation);
        this.updateReservation(previousReservation, refundValue, ReservationStatus.RESCHEDULED);

        ReservationDTO newReservation = this.bookReservation(CreateReservationRequestDTO.builder()
                .guestId(previousReservation.getGuest().getId())
                .scheduleId(scheduleId)
                .build());
        newReservation.setPreviousReservation(reservationMapper.map(previousReservation));
        return newReservation;
    }

    List<ScheduleDTO> findAvailableSchedulesByDate(LocalDateTime startDate, LocalDateTime endDate) {
        List<ScheduleDTO> schedules = scheduleService.findSchedulesByDates(startDate, endDate);
        if (schedules.isEmpty()) {
            return Collections.emptyList();
        }
        schedules.removeAll(
                reservationMapper.map(reservationRepository.findByReservationStatusAndSchedule_StartDateTimeGreaterThanEqualAndSchedule_EndDateTimeLessThanEqual(
                                        ReservationStatus.READY_TO_PLAY, startDate, endDate))
                        .stream()
                        .map(ReservationDTO::getSchedule)
                        .collect(Collectors.toList()));
        return schedules;
    }

}
