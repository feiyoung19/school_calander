package com.calendarsystem.controller;

import com.calendarsystem.model.AcademicYear;
import com.calendarsystem.model.Event;
import com.calendarsystem.model.Holiday;
import com.calendarsystem.repository.AcademicYearRepository;
import com.calendarsystem.repository.HolidayRepository;
import com.calendarsystem.repository.SemesterRepository;
import com.calendarsystem.service.CalendarService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final CalendarService calendarService;
    private final AcademicYearRepository yearRepo;
    private final SemesterRepository semesterRepo;
    private final HolidayRepository holidayRepo;

    @PostMapping("/academic-year")
    public ResponseEntity<AcademicYear> createYear(@RequestParam String name, @RequestParam int startYear) {
        return ResponseEntity.ok(calendarService.createAcademicYear(name, startYear));
    }

    @PostMapping("/holiday")
    public ResponseEntity<Holiday> addHoliday(@RequestBody Holiday holiday, @RequestParam Long yearId) {
        AcademicYear year = yearRepo.findById(yearId).orElseThrow();
        holiday.setAcademicYear(year);
        return ResponseEntity.ok(holidayRepo.save(holiday));
    }

    @PostMapping("/event")
    public ResponseEntity<?> addEvent(@RequestBody Event event, @RequestParam Long semesterId) {
        try {
            Event saved = calendarService.addEvent(event, semesterId);
            return ResponseEntity.ok(saved);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/publish/{yearId}")
    public ResponseEntity<Void> publish(@PathVariable Long yearId) {
        calendarService.publishYear(yearId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/semesters")
    public ResponseEntity<?> getSemesters(@RequestParam Long yearId) {
        return ResponseEntity.ok(semesterRepo.findByAcademicYearId(yearId));
    }
}
