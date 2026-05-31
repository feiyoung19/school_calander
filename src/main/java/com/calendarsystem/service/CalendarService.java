package com.calendarsystem.service;

import com.calendarsystem.model.*;
import com.calendarsystem.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@RequiredArgsConstructor
public class CalendarService {

    private final AcademicYearRepository yearRepo;
    private final SemesterRepository semesterRepo;
    private final HolidayRepository holidayRepo;
    private final EventRepository eventRepo;
    private final ConflictDetector conflictDetector;

    @Transactional
    public AcademicYear createAcademicYear(String name, int startYear) {
        AcademicYear year = new AcademicYear();
        year.setName(name);
        year.setStartDate(LocalDate.of(startYear, 9, 1));
        year.setEndDate(LocalDate.of(startYear + 1, 8, 31));
        year = yearRepo.save(year);

        Semester fall = new Semester();
        fall.setTerm("FALL");
        fall.setStartDate(LocalDate.of(startYear, 9, 1));
        fall.setEndDate(LocalDate.of(startYear + 1, 1, 15));
        fall.setWeekCount(calculateWeeks(fall.getStartDate(), fall.getEndDate()));
        fall.setAcademicYear(year);
        semesterRepo.save(fall);

        Semester spring = new Semester();
        spring.setTerm("SPRING");
        spring.setStartDate(LocalDate.of(startYear + 1, 2, 20));
        spring.setEndDate(LocalDate.of(startYear + 1, 7, 10));
        spring.setWeekCount(calculateWeeks(spring.getStartDate(), spring.getEndDate()));
        spring.setAcademicYear(year);
        semesterRepo.save(spring);

        return year;
    }

    public List<Map<String, Object>> getCalendarEvents(Long yearId) {
        List<Map<String, Object>> events = new ArrayList<>();
        List<Semester> semesters = semesterRepo.findByAcademicYearId(yearId);
        List<Holiday> holidays = holidayRepo.findByAcademicYearId(yearId);

        for (Semester sem : semesters) {
            Map<String, Object> semEvent = new HashMap<>();
            semEvent.put("title", sem.getTerm().equals("FALL") ? "秋季学期" : "春季学期");
            semEvent.put("start", sem.getStartDate().toString());
            semEvent.put("end", sem.getEndDate().plusDays(1).toString());
            semEvent.put("color", "#e3f2fd");
            semEvent.put("display", "background");
            events.add(semEvent);

            List<Event> termEvents = eventRepo.findBySemesterId(sem.getId());
            for (Event e : termEvents) {
                Map<String, Object> ev = new HashMap<>();
                ev.put("title", e.getTitle());
                ev.put("start", e.getStartDate().toString());
                ev.put("end", e.getEndDate().plusDays(1).toString());
                ev.put("color", getEventColor(e.getEventType()));
                ev.put("extendedProps", Map.of("description", e.getDescription() != null ? e.getDescription() : ""));
                events.add(ev);
            }
        }

        for (Holiday h : holidays) {
            Map<String, Object> hol = new HashMap<>();
            hol.put("title", h.getName());
            hol.put("start", h.getStartDate().toString());
            hol.put("end", h.getEndDate().plusDays(1).toString());
            hol.put("color", h.getType() == Holiday.HolidayType.ADJUSTMENT_WORK ? "#ffb74d" : "#f44336");
            if (h.getType() != Holiday.HolidayType.ADJUSTMENT_WORK) {
                hol.put("display", "background");
            }
            events.add(hol);
        }

        return events;
    }

    @Transactional
    public Event addEvent(Event event, Long semesterId) {
        Semester sem = semesterRepo.findById(semesterId).orElseThrow();
        event.setSemester(sem);
        event.setWeekNumber(calculateWeekNumber(event.getStartDate(), sem));

        List<Holiday> holidays = holidayRepo.findByAcademicYearId(sem.getAcademicYear().getId());
        List<Event> existing = eventRepo.findBySemesterId(semesterId);
        List<String> conflicts = conflictDetector.checkEventConflict(event, holidays, existing);
        if (!conflicts.isEmpty()) {
            throw new RuntimeException("冲突警告：" + String.join(", ", conflicts));
        }
        return eventRepo.save(event);
    }

    @Transactional
    public AcademicYear publishYear(Long yearId) {
        AcademicYear year = yearRepo.findById(yearId).orElseThrow();
        year.setStatus(AcademicYear.Status.PUBLISHED);
        return yearRepo.save(year);
    }

    private int calculateWeeks(LocalDate start, LocalDate end) {
        LocalDate firstMonday = start.with(DayOfWeek.MONDAY);
        LocalDate lastSunday = end.with(DayOfWeek.SUNDAY);
        return (int) ChronoUnit.WEEKS.between(firstMonday, lastSunday) + 1;
    }

    private int calculateWeekNumber(LocalDate date, Semester sem) {
        LocalDate firstMonday = sem.getStartDate().with(DayOfWeek.MONDAY);
        if (sem.getStartDate().getDayOfWeek() != DayOfWeek.MONDAY) {
            firstMonday = firstMonday.plusWeeks(1);
        }
        return (int) ChronoUnit.WEEKS.between(firstMonday, date) + 1;
    }

    private String getEventColor(Event.EventType type) {
        return switch (type) {
            case SPORTS -> "#4caf50";
            case EXAM -> "#ff9800";
            case CEREMONY -> "#2196f3";
            case OTHER -> "#9c27b0";
        };
    }
}
