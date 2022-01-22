package com.tenniscourts.schedules;

import com.tenniscourts.exceptions.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;

    private final ScheduleMapper scheduleMapper;

    public ScheduleDTO addSchedule(CreateScheduleRequestDTO createScheduleRequestDTO) {
        Schedule schedule = scheduleMapper.map(createScheduleRequestDTO);
        schedule.setEndDateTime(schedule.getStartDateTime().plusHours(1));
        List<Schedule> existingSchedules = scheduleRepository.findByTennisCourt_IdAndStartDateTimeBetween(createScheduleRequestDTO.getTennisCourtId(), schedule.getStartDateTime(), schedule.getEndDateTime());
        if (!existingSchedules.isEmpty()) {
            throw new IllegalArgumentException("This schedule already exists.");
        }
        return scheduleMapper.map(scheduleRepository.saveAndFlush(schedule));
    }

    public List<ScheduleDTO> findSchedulesByDates(LocalDateTime startDate, LocalDateTime endDate) {
        return scheduleMapper.map(scheduleRepository.findByStartDateTimeBetweenOrderByStartDateTime(startDate, endDate));
    }

    public ScheduleDTO findSchedule(Long scheduleId) {
        return scheduleRepository.findById(scheduleId).map(scheduleMapper::map).orElseThrow(() -> {
            throw new EntityNotFoundException("Schedule not found.");
        });
    }

    public List<ScheduleDTO> findSchedulesByTennisCourtId(Long tennisCourtId) {
        return scheduleMapper.map(scheduleRepository.findByTennisCourt_IdOrderByStartDateTime(tennisCourtId));
    }
}
