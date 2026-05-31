package com.calendarsystem.controller;

import com.calendarsystem.model.AcademicYear;
import com.calendarsystem.model.Event;
import com.calendarsystem.model.Holiday;
import com.calendarsystem.model.Semester;
import com.calendarsystem.repository.AcademicYearRepository;
import com.calendarsystem.repository.EventRepository;
import com.calendarsystem.repository.HolidayRepository;
import com.calendarsystem.repository.SemesterRepository;
import com.calendarsystem.service.CalendarService;
import com.calendarsystem.service.ICalService;
import com.calendarsystem.service.NaturalYearService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class CalendarController {

    private final CalendarService calendarService;
    private final ICalService icalService;
    private final NaturalYearService naturalYearService;
    private final AcademicYearRepository yearRepo;
    private final HolidayRepository holidayRepo;
    private final SemesterRepository semesterRepo;
    private final EventRepository eventRepo;

    @GetMapping("/")
    public String showCalendar(Model model) {
        List<AcademicYear> years = yearRepo.findAll();
        model.addAttribute("years", years);
        model.addAttribute("currentYearId", getCurrentYearId());
        return "index";
    }

    @GetMapping("/admin")
    public String showAdmin(Model model) {
        List<AcademicYear> years = yearRepo.findAll();
        model.addAttribute("years", years);
        model.addAttribute("currentYearId", getCurrentYearId());
        return "admin";
    }

    @GetMapping("/dashboard")
    public String showDashboard(Model model) {
        List<AcademicYear> years = yearRepo.findAll();
        model.addAttribute("years", years);
        model.addAttribute("currentYearId", getCurrentYearId());
        return "dashboard";
    }

    private Long getCurrentYearId() {
        LocalDate today = LocalDate.now();
        List<AcademicYear> years = yearRepo.findAll();
        
        Optional<AcademicYear> currentYear = years.stream()
                .filter(y -> !today.isBefore(y.getStartDate()) && !today.isAfter(y.getEndDate()))
                .findFirst();
        
        if (currentYear.isPresent()) {
            return currentYear.get().getId();
        }
        
        Optional<AcademicYear> nearestYear = years.stream()
                .filter(y -> y.getStartDate().isAfter(today))
                .min((y1, y2) -> y1.getStartDate().compareTo(y2.getStartDate()));
        
        if (nearestYear.isPresent()) {
            return nearestYear.get().getId();
        }
        
        Optional<AcademicYear> latestYear = years.stream()
                .max((y1, y2) -> y1.getStartDate().compareTo(y2.getStartDate()));
        
        return latestYear.map(AcademicYear::getId).orElse(null);
    }

    @GetMapping("/api/events")
    @ResponseBody
    public List<Map<String, Object>> getEvents(@RequestParam Long yearId) {
        return calendarService.getCalendarEvents(yearId);
    }

    @GetMapping("/api/academic-years")
    @ResponseBody
    public List<AcademicYear> getAcademicYears() {
        return yearRepo.findAll();
    }

    @GetMapping("/api/holidays")
    @ResponseBody
    public List<Holiday> getHolidays(@RequestParam Long yearId) {
        return holidayRepo.findByAcademicYearId(yearId);
    }

    @GetMapping("/api/semesters")
    @ResponseBody
    public List<?> getSemesters(@RequestParam Long yearId) {
        return calendarService.getCalendarEvents(yearId).stream()
                .filter(e -> e.containsKey("display") && "background".equals(e.get("display")))
                .toList();
    }

    @GetMapping(value = "/api/calendar/ical", produces = "text/calendar; charset=utf-8")
    @ResponseBody
    public ResponseEntity<?> getICalendar(@RequestParam Long yearId) {
        try {
            String ics = icalService.generateCalendarIcs(yearId, calendarService);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"university-calendar.ics\"")
                    .contentType(MediaType.parseMediaType("text/calendar; charset=utf-8"))
                    .body(ics);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .contentType(MediaType.TEXT_PLAIN)
                    .body("iCal生成失败: " + e.getMessage());
        }
    }

    @GetMapping("/api/natural-year/info")
    @ResponseBody
    public Map<String, Object> getNaturalYearInfo(@RequestParam(required = false) Integer year) {
        if (year == null) {
            year = naturalYearService.getCurrentNaturalYear();
        }

        NaturalYearService.NaturalYearRange range = naturalYearService.getNaturalYearRange(year);

        Map<String, Object> result = new HashMap<>();
        result.put("year", year);
        result.put("name", range.getName());
        result.put("startDate", range.getStartDate().toString());
        result.put("endDate", range.getEndDate().toString());
        result.put("springFestivalCurrent", naturalYearService.getSpringFestivalDate(year).toString());
        result.put("springFestivalNext", naturalYearService.getSpringFestivalDate(year + 1).toString());

        return result;
    }

    @GetMapping("/api/natural-year/events")
    @ResponseBody
    public List<Map<String, Object>> getNaturalYearEvents(@RequestParam(required = false) Integer year) {
        if (year == null) {
            year = naturalYearService.getCurrentNaturalYear();
        }

        NaturalYearService.NaturalYearRange range = naturalYearService.getNaturalYearRange(year);
        LocalDate startDate = range.getStartDate();
        LocalDate endDate = range.getEndDate();

        List<Map<String, Object>> allEvents = new ArrayList<>();

        List<AcademicYear> academicYears = yearRepo.findAll();
        for (AcademicYear academicYear : academicYears) {
            List<Semester> semesters = semesterRepo.findByAcademicYearId(academicYear.getId());
            for (Semester semester : semesters) {
                if (semester.getStartDate() != null && semester.getEndDate() != null) {
                    boolean overlaps = !semester.getEndDate().isBefore(startDate) 
                            && !semester.getStartDate().isAfter(endDate);
                    if (overlaps) {
                        List<Event> events = eventRepo.findBySemesterId(semester.getId());
                        for (Event event : events) {
                            if (event.getStartDate() != null) {
                                boolean eventInRange = !event.getStartDate().isBefore(startDate) 
                                        && !event.getStartDate().isAfter(endDate);
                                if (eventInRange) {
                                    Map<String, Object> eventMap = new HashMap<>();
                                    eventMap.put("id", "event-" + event.getId());
                                    eventMap.put("title", event.getTitle());
                                    eventMap.put("start", event.getStartDate().toString());
                                    if (event.getEndDate() != null) {
                                        eventMap.put("end", event.getEndDate().toString());
                                    }
                                    eventMap.put("eventType", event.getEventType() != null ? event.getEventType().name() : "OTHER");
                                    eventMap.put("color", getEventColor(event.getEventType()));
                                    allEvents.add(eventMap);
                                }
                            }
                        }
                    }
                }
            }

            List<Holiday> holidays = holidayRepo.findByAcademicYearId(academicYear.getId());
            for (Holiday holiday : holidays) {
                if (holiday.getStartDate() != null) {
                    boolean holidayInRange = !holiday.getStartDate().isBefore(startDate) 
                            && !holiday.getStartDate().isAfter(endDate);
                    if (holidayInRange) {
                        Map<String, Object> holidayMap = new HashMap<>();
                        holidayMap.put("id", "holiday-" + holiday.getId());
                        holidayMap.put("title", holiday.getName());
                        holidayMap.put("start", holiday.getStartDate().toString());
                        if (holiday.getEndDate() != null) {
                            holidayMap.put("end", holiday.getEndDate().toString());
                        }
                        holidayMap.put("display", "background");
                        holidayMap.put("color", getHolidayColor(holiday.getType()));
                        allEvents.add(holidayMap);
                    }
                }
            }
        }

        allEvents.sort((e1, e2) -> {
            String start1 = (String) e1.get("start");
            String start2 = (String) e2.get("start");
            return start1.compareTo(start2);
        });

        return allEvents;
    }

    private String getEventColor(Event.EventType type) {
        if (type == null) return "#2196f3";
        switch (type) {
            case SPORTS: return "#ff9800";
            case EXAM: return "#f44336";
            case CEREMONY: return "#4caf50";
            default: return "#2196f3";
        }
    }

    private String getHolidayColor(Holiday.HolidayType type) {
        if (type == null) return "#9e9e9e";
        switch (type) {
            case LEGAL: return "#f44336";
            case SCHOOL: return "#2196f3";
            default: return "#9e9e9e";
        }
    }

    @GetMapping(value = "/api/natural-year/ical", produces = "text/calendar; charset=utf-8")
    @ResponseBody
    public ResponseEntity<?> getNaturalYearICalendar(@RequestParam(required = false) Integer year) {
        if (year == null) {
            year = naturalYearService.getCurrentNaturalYear();
        }

        try {
            NaturalYearService.NaturalYearRange range = naturalYearService.getNaturalYearRange(year);
            List<Map<String, Object>> events = getNaturalYearEvents(year);

            StringBuilder ics = new StringBuilder();
            ics.append("BEGIN:VCALENDAR\n");
            ics.append("VERSION:2.0\n");
            ics.append("PRODID:-//University Calendar System//EN\n");
            ics.append("CALSCALE:GREGORIAN\n");
            ics.append("METHOD:PUBLISH\n");
            ics.append("X-WR-CALNAME:").append(range.getName()).append("校历\n");

            for (Map<String, Object> event : events) {
                ics.append("BEGIN:VEVENT\n");
                ics.append("DTSTART:").append(formatDateForIcs((String) event.get("start"))).append("\n");
                if (event.get("end") != null) {
                    ics.append("DTEND:").append(formatDateForIcs((String) event.get("end"))).append("\n");
                }
                ics.append("SUMMARY:").append(event.get("title")).append("\n");
                ics.append("END:VEVENT\n");
            }

            ics.append("END:VCALENDAR\n");

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                            "attachment; filename=\"natural-year-" + year + "-calendar.ics\"")
                    .contentType(MediaType.parseMediaType("text/calendar; charset=utf-8"))
                    .body(ics.toString());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .contentType(MediaType.TEXT_PLAIN)
                    .body("iCal生成失败: " + e.getMessage());
        }
    }

    private String formatDateForIcs(String dateStr) {
        if (dateStr == null) return "";
        return dateStr.replace("-", "");
    }
}
