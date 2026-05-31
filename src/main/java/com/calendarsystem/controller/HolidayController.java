package com.calendarsystem.controller;

import com.calendarsystem.model.Holiday;
import com.calendarsystem.repository.AcademicYearRepository;
import com.calendarsystem.repository.HolidayRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/holidays")
@RequiredArgsConstructor
public class HolidayController {

    private final HolidayRepository holidayRepo;
    private final AcademicYearRepository yearRepo;

    @GetMapping("/by-year/{yearId}")
    public ResponseEntity<List<Holiday>> getHolidaysByYear(@PathVariable Long yearId) {
        return ResponseEntity.ok(holidayRepo.findByAcademicYearId(yearId));
    }

    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createHoliday(@RequestBody Map<String, Object> holidayData) {
        try {
            Long yearId = ((Number) holidayData.get("yearId")).longValue();
            var yearOpt = yearRepo.findById(yearId);
            if (yearOpt.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "学年不存在"));
            }

            Holiday holiday = new Holiday();
            holiday.setName((String) holidayData.get("name"));
            holiday.setType(Holiday.HolidayType.valueOf((String) holidayData.get("type")));
            holiday.setStartDate(LocalDate.parse((String) holidayData.get("startDate")));
            holiday.setEndDate(LocalDate.parse((String) holidayData.get("endDate")));
            holiday.setAcademicYear(yearOpt.get());
            holiday.setRecurring((Boolean) holidayData.get("recurring"));

            holiday = holidayRepo.save(holiday);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "节假日创建成功",
                    "holiday", holiday
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("success", false, "message", "创建失败: " + e.getMessage()));
        }
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<Map<String, Object>> updateHoliday(
            @PathVariable Long id,
            @RequestBody Map<String, Object> holidayData) {
        try {
            Holiday holiday = holidayRepo.findById(id)
                    .orElseThrow(() -> new RuntimeException("节假日不存在"));

            holiday.setName((String) holidayData.get("name"));
            holiday.setType(Holiday.HolidayType.valueOf((String) holidayData.get("type")));
            holiday.setStartDate(LocalDate.parse((String) holidayData.get("startDate")));
            holiday.setEndDate(LocalDate.parse((String) holidayData.get("endDate")));
            holiday.setRecurring((Boolean) holidayData.get("recurring"));

            holiday = holidayRepo.save(holiday);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "节假日更新成功",
                    "holiday", holiday
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("success", false, "message", "更新失败: " + e.getMessage()));
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Map<String, Object>> deleteHoliday(@PathVariable Long id) {
        try {
            holidayRepo.deleteById(id);
            return ResponseEntity.ok(Map.of("success", true, "message", "删除成功"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("success", false, "message", "删除失败: " + e.getMessage()));
        }
    }

    @PostMapping("/generate-chinese-holidays")
    public ResponseEntity<Map<String, Object>> generateChineseHolidays(
            @RequestParam Long yearId,
            @RequestParam int year) {
        try {
            var yearOpt = yearRepo.findById(yearId);
            if (yearOpt.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "学年不存在"));
            }

            int count = 0;

            count += createHolidayIfNotExists(yearOpt.get(), "元旦", year + "-01-01", year + "-01-01", Holiday.HolidayType.LEGAL, true);
            count += createHolidayIfNotExists(yearOpt.get(), "春节", year + "-01-28", year + "-02-03", Holiday.HolidayType.LEGAL, true);
            count += createHolidayIfNotExists(yearOpt.get(), "清明节", year + "-04-04", year + "-04-06", Holiday.HolidayType.LEGAL, true);
            count += createHolidayIfNotExists(yearOpt.get(), "劳动节", year + "-05-01", year + "-05-03", Holiday.HolidayType.LEGAL, true);
            count += createHolidayIfNotExists(yearOpt.get(), "端午节", year + "-05-28", year + "-05-30", Holiday.HolidayType.LEGAL, true);
            count += createHolidayIfNotExists(yearOpt.get(), "中秋节", year + "-09-15", year + "-09-17", Holiday.HolidayType.LEGAL, true);
            count += createHolidayIfNotExists(yearOpt.get(), "国庆节", year + "-10-01", year + "-10-07", Holiday.HolidayType.LEGAL, true);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "成功生成 " + count + " 个法定节假日",
                    "count", count
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("success", false, "message", "生成失败: " + e.getMessage()));
        }
    }

    private int createHolidayIfNotExists(
            com.calendarsystem.model.AcademicYear year,
            String name,
            String startDate,
            String endDate,
            Holiday.HolidayType type,
            boolean recurring) {
        
        List<Holiday> existing = holidayRepo.findByAcademicYearId(year.getId());
        boolean exists = existing.stream()
                .anyMatch(h -> h.getName().equals(name));

        if (!exists) {
            Holiday holiday = new Holiday();
            holiday.setName(name);
            holiday.setType(type);
            holiday.setStartDate(LocalDate.parse(startDate));
            holiday.setEndDate(LocalDate.parse(endDate));
            holiday.setAcademicYear(year);
            holiday.setRecurring(recurring);
            holidayRepo.save(holiday);
            return 1;
        }
        return 0;
    }
}
