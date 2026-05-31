package com.calendarsystem;

import com.calendarsystem.model.*;
import com.calendarsystem.repository.*;
import com.calendarsystem.service.CalendarService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class CalendarSystemApplicationTests {

    @Autowired
    private AcademicYearRepository yearRepo;

    @Autowired
    private SemesterRepository semesterRepo;

    @Autowired
    private HolidayRepository holidayRepo;

    @Autowired
    private EventRepository eventRepo;

    @Autowired
    private CalendarService calendarService;

    @BeforeEach
    void setUp() {
        yearRepo.deleteAll();
        semesterRepo.deleteAll();
        holidayRepo.deleteAll();
        eventRepo.deleteAll();
    }

    @Test
    void contextLoads() {
        assertNotNull(yearRepo);
        assertNotNull(semesterRepo);
        assertNotNull(holidayRepo);
        assertNotNull(eventRepo);
        assertNotNull(calendarService);
    }

    @Test
    void testCreateAcademicYear() {
        AcademicYear year = new AcademicYear();
        year.setName("2025-2026");
        year.setStartDate(LocalDate.of(2025, 9, 1));
        year.setEndDate(LocalDate.of(2026, 8, 31));
        year.setStatus(AcademicYear.Status.DRAFT);

        AcademicYear saved = yearRepo.save(year);

        assertNotNull(saved.getId());
        assertEquals("2025-2026", saved.getName());
    }

    @Test
    void testCreateSemester() {
        AcademicYear year = new AcademicYear();
        year.setName("2025-2026");
        year.setStartDate(LocalDate.of(2025, 9, 1));
        year.setEndDate(LocalDate.of(2026, 8, 31));
        year.setStatus(AcademicYear.Status.DRAFT);
        year = yearRepo.save(year);

        Semester semester = new Semester();
        semester.setTerm("FALL");
        semester.setStartDate(LocalDate.of(2025, 9, 1));
        semester.setEndDate(LocalDate.of(2026, 1, 15));
        semester.setWeekCount(19);
        semester.setAcademicYear(year);

        Semester saved = semesterRepo.save(semester);

        assertNotNull(saved.getId());
        assertEquals("FALL", saved.getTerm());
    }

    @Test
    void testCreateEventConflictDetection() {
        AcademicYear year = calendarService.createAcademicYear("2026-2027", 2026);
        List<Semester> semesters = semesterRepo.findByAcademicYearId(year.getId());
        assertEquals(2, semesters.size());

        Event event = new Event();
        event.setTitle("开学典礼");
        event.setEventType(Event.EventType.CEREMONY);
        event.setStartDate(LocalDate.of(2026, 9, 1));
        event.setEndDate(LocalDate.of(2026, 9, 1));
        event.setStatus(Event.Status.CONFIRMED);
        event.setPriority(1);

        Event saved = calendarService.addEvent(event, semesters.get(0).getId());

        assertNotNull(saved.getId());
        assertEquals("开学典礼", saved.getTitle());
    }

    @Test
    void testGetCalendarEvents() {
        AcademicYear year = calendarService.createAcademicYear("2026-2027", 2026);
        
        List<Map<String, Object>> events = calendarService.getCalendarEvents(year.getId());

        assertNotNull(events);
        assertFalse(events.isEmpty());
    }

    @Test
    void testPublishAcademicYear() {
        AcademicYear year = calendarService.createAcademicYear("2026-2027", 2026);
        assertEquals(AcademicYear.Status.DRAFT, year.getStatus());

        AcademicYear published = calendarService.publishYear(year.getId());
        assertEquals(AcademicYear.Status.PUBLISHED, published.getStatus());
    }
}