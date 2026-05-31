package com.calendarsystem.service;

import com.calendarsystem.model.Event;
import com.calendarsystem.model.Holiday;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class ConflictDetector {

    public List<String> checkEventConflict(Event newEvent, List<Holiday> holidays, List<Event> existingEvents) {
        List<String> warnings = new ArrayList<>();
        LocalDate start = newEvent.getStartDate();
        LocalDate end = newEvent.getEndDate();

        for (Holiday h : holidays) {
            if (h.getType() != Holiday.HolidayType.ADJUSTMENT_WORK &&
                    dateRangesOverlap(start, end, h.getStartDate(), h.getEndDate())) {
                warnings.add("与" + h.getName() + "（假日）重叠");
            }
        }

        for (Event e : existingEvents) {
            if (newEvent.getId() == null || !e.getId().equals(newEvent.getId())) {
                if (e.getStatus() == Event.Status.CONFIRMED &&
                        dateRangesOverlap(start, end, e.getStartDate(), e.getEndDate())) {
                    warnings.add("与活动【" + e.getTitle() + "】时间冲突");
                }
            }
        }
        return warnings;
    }

    private boolean dateRangesOverlap(LocalDate s1, LocalDate e1, LocalDate s2, LocalDate e2) {
        return !s1.isAfter(e2) && !s2.isAfter(e1);
    }
}
