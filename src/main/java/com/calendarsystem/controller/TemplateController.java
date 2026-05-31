package com.calendarsystem.controller;

import com.calendarsystem.model.*;
import com.calendarsystem.repository.*;
import com.calendarsystem.service.CalendarService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@RestController
@RequestMapping("/api/template")
@RequiredArgsConstructor
public class TemplateController {

    private final AcademicYearRepository yearRepo;
    private final SemesterRepository semesterRepo;
    private final HolidayRepository holidayRepo;
    private final EventRepository eventRepo;
    private final CalendarService calendarService;
    private final ObjectMapper objectMapper;

    @GetMapping("/export/{yearId}")
    public ResponseEntity<String> exportYearTemplate(@PathVariable Long yearId) {
        try {
            AcademicYear year = yearRepo.findById(yearId)
                    .orElseThrow(() -> new RuntimeException("学年不存在"));

            List<Semester> semesters = semesterRepo.findByAcademicYearId(yearId);
            List<Holiday> holidays = holidayRepo.findByAcademicYearId(yearId);
            List<Event> events = new ArrayList<>();
            for (Semester sem : semesters) {
                events.addAll(eventRepo.findBySemesterId(sem.getId()));
            }

            Map<String, Object> template = new HashMap<>();
            template.put("name", year.getName());
            template.put("startDate", year.getStartDate());
            template.put("endDate", year.getEndDate());
            template.put("semesters", semesters);
            template.put("holidays", holidays);
            template.put("events", events);
            template.put("exportTime", new Date());
            template.put("version", "1.0");

            String json = objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(template);

            String filename = "学年模板_" + year.getName() + "_" + System.currentTimeMillis() + ".json";

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(json);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("导出失败: " + e.getMessage());
        }
    }

    @PostMapping("/import")
    public ResponseEntity<Map<String, Object>> importYearTemplate(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "yearOffset", defaultValue = "0") int yearOffset) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "请选择文件"));
            }

            String content = new String(file.getBytes(), "UTF-8");
            Map<String, Object> template = objectMapper.readValue(content, Map.class);

            String originalName = (String) template.get("name");
            String newName = adjustYearInName(originalName, yearOffset);

            AcademicYear newYear = new AcademicYear();
            newYear.setName(newName);
            newYear.setStartDate(adjustDate((String) template.get("startDate"), yearOffset));
            newYear.setEndDate(adjustDate((String) template.get("endDate"), yearOffset));
            newYear.setStatus(AcademicYear.Status.DRAFT);
            newYear = yearRepo.save(newYear);

            List<Map<String, Object>> semestersData = (List<Map<String, Object>>) template.get("semesters");
            Map<Long, Long> semesterIdMapping = new HashMap<>();

            for (Map<String, Object> semData : semestersData) {
                Semester sem = new Semester();
                sem.setTerm((String) semData.get("term"));
                sem.setStartDate(adjustDate((String) semData.get("startDate"), yearOffset));
                sem.setEndDate(adjustDate((String) semData.get("endDate"), yearOffset));
                sem.setWeekCount((Integer) semData.get("weekCount"));
                sem.setAcademicYear(newYear);
                sem = semesterRepo.save(sem);
                semesterIdMapping.put(((Number) semData.get("id")).longValue(), sem.getId());
            }

            List<Map<String, Object>> holidaysData = (List<Map<String, Object>>) template.get("holidays");
            for (Map<String, Object> holData : holidaysData) {
                Holiday holiday = new Holiday();
                holiday.setName((String) holData.get("name"));
                holiday.setType(Holiday.HolidayType.valueOf((String) holData.get("type")));
                holiday.setStartDate(adjustDate((String) holData.get("startDate"), yearOffset));
                holiday.setEndDate(adjustDate((String) holData.get("endDate"), yearOffset));
                holiday.setAcademicYear(newYear);
                holiday.setRecurring((Boolean) holData.get("recurring"));
                holidayRepo.save(holiday);
            }

            List<Map<String, Object>> eventsData = (List<Map<String, Object>>) template.get("events");
            for (Map<String, Object> evtData : eventsData) {
                Event event = new Event();
                event.setTitle((String) evtData.get("title"));
                event.setEventType(Event.EventType.valueOf((String) evtData.get("eventType")));
                event.setStartDate(adjustDate((String) evtData.get("startDate"), yearOffset));
                event.setEndDate(adjustDate((String) evtData.get("endDate"), yearOffset));
                event.setStatus(Event.Status.valueOf((String) evtData.get("status")));
                event.setPriority((Integer) evtData.get("priority"));
                if (evtData.get("description") != null) {
                    event.setDescription((String) evtData.get("description"));
                }
                if (evtData.get("weekNumber") != null) {
                    event.setWeekNumber((Integer) evtData.get("weekNumber"));
                }

                Long oldSemId = ((Number) evtData.get("semesterId")).longValue();
                Long newSemId = semesterIdMapping.get(oldSemId);
                if (newSemId != null) {
                    Semester sem = semesterRepo.findById(newSemId).orElse(null);
                    event.setSemester(sem);
                    eventRepo.save(event);
                }
            }

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "导入成功");
            result.put("yearId", newYear.getId());
            result.put("yearName", newYear.getName());

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body(Map.of("success", false, "message", "导入失败: " + e.getMessage()));
        }
    }

    private String adjustYearInName(String originalName, int yearOffset) {
        if (originalName == null || !originalName.matches("\\d{4}-\\d{4}")) {
            return originalName;
        }
        String[] parts = originalName.split("-");
        int startYear = Integer.parseInt(parts[0]) + yearOffset;
        int endYear = Integer.parseInt(parts[1]) + yearOffset;
        return startYear + "-" + endYear;
    }

    private java.time.LocalDate adjustDate(String dateStr, int yearOffset) {
        if (dateStr == null) {
            return java.time.LocalDate.now();
        }
        java.time.LocalDate date = java.time.LocalDate.parse(dateStr);
        return date.plusYears(yearOffset);
    }

    @GetMapping("/list")
    public ResponseEntity<List<Map<String, Object>>> listTemplates() {
        List<AcademicYear> years = yearRepo.findAll();
        List<Map<String, Object>> templates = new ArrayList<>();
        for (AcademicYear year : years) {
            Map<String, Object> info = new HashMap<>();
            info.put("id", year.getId());
            info.put("name", year.getName());
            info.put("startDate", year.getStartDate());
            info.put("endDate", year.getEndDate());
            info.put("status", year.getStatus());
            templates.add(info);
        }
        return ResponseEntity.ok(templates);
    }
}
