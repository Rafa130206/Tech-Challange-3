package com.fiapon.scheduling.availability;

import com.fiapon.scheduling.dto.availability.AvailableSlotsResponse;
import com.fiapon.scheduling.model.UserRole;
import com.fiapon.scheduling.repository.AppointmentRepository;
import com.fiapon.scheduling.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AvailabilityService {

    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;
    private final WorkingHoursSlotGenerator slotGenerator;

    public AvailabilityService(AppointmentRepository appointmentRepository,
                               UserRepository userRepository,
                               WorkingHoursSlotGenerator slotGenerator) {
        this.appointmentRepository = appointmentRepository;
        this.userRepository = userRepository;
        this.slotGenerator = slotGenerator;
    }

    public AvailableSlotsResponse getAvailability(Long doctorId, LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

        Set<LocalTime> bookedTimes = appointmentRepository
                .findByDoctorIdAndDateTimeBetween(doctorId, startOfDay, endOfDay)
                .stream()
                .map(a -> a.getDateTime().toLocalTime())
                .collect(Collectors.toSet());

        List<LocalTime> availableSlots = slotGenerator.generate().stream()
                .filter(slot -> !bookedTimes.contains(slot))
                .toList();

        return new AvailableSlotsResponse(doctorId, date, availableSlots);
    }

    public List<AvailableSlotsResponse> getAvailabilityForAllDoctors(LocalDate date) {
        return userRepository.findByRole(UserRole.DOCTOR).stream()
                .map(doctor -> getAvailability(doctor.getId(), date))
                .toList();
    }
}