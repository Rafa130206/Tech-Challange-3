package com.fiapon.scheduling.availability;

import com.fiapon.scheduling.dto.availability.AvailableSlotsResponse;
import com.fiapon.scheduling.model.Appointment;
import com.fiapon.scheduling.model.AppointmentStatus;
import com.fiapon.scheduling.model.UserRole;
import com.fiapon.scheduling.repository.AppointmentRepository;
import com.fiapon.scheduling.repository.UserRepository;
import com.fiapon.scheduling.validation.DoctorAvailabilityValidator;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

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
        long gapHours = DoctorAvailabilityValidator.MINIMUM_GAP_HOURS;
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

        // Fetch a bit before/after the day too, so an appointment near midnight
        // still blocks the slots within the minimum gap on this date.
        List<LocalDateTime> bookedDateTimes = appointmentRepository
                .findByDoctorIdAndDateTimeBetweenAndStatusNot(
                        doctorId, startOfDay.minusHours(gapHours), endOfDay.plusHours(gapHours), AppointmentStatus.CANCELLED)
                .stream()
                .map(Appointment::getDateTime)
                .toList();

        List<LocalTime> availableSlots = slotGenerator.generate().stream()
                .filter(slot -> {
                    LocalDateTime slotDateTime = date.atTime(slot);
                    LocalDateTime windowStart = slotDateTime.minusHours(gapHours);
                    LocalDateTime windowEnd = slotDateTime.plusHours(gapHours);
                    return bookedDateTimes.stream().noneMatch(
                            booked -> !booked.isBefore(windowStart) && !booked.isAfter(windowEnd));
                })
                .toList();

        return new AvailableSlotsResponse(doctorId, date, availableSlots);
    }

    public List<AvailableSlotsResponse> getAvailabilityForAllDoctors(LocalDate date) {
        return userRepository.findByRole(UserRole.DOCTOR).stream()
                .map(doctor -> getAvailability(doctor.getId(), date))
                .toList();
    }
}