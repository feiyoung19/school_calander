package com.calendarsystem.service;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Service
public class NaturalYearService {

    private static final Map<Integer, LocalDate> SPRING_FESTIVAL_DATES = new HashMap<>();

    static {
        SPRING_FESTIVAL_DATES.put(2024, LocalDate.of(2024, 2, 10));
        SPRING_FESTIVAL_DATES.put(2025, LocalDate.of(2025, 1, 29));
        SPRING_FESTIVAL_DATES.put(2026, LocalDate.of(2026, 2, 17));
        SPRING_FESTIVAL_DATES.put(2027, LocalDate.of(2027, 2, 6));
        SPRING_FESTIVAL_DATES.put(2028, LocalDate.of(2028, 1, 26));
        SPRING_FESTIVAL_DATES.put(2029, LocalDate.of(2029, 2, 13));
        SPRING_FESTIVAL_DATES.put(2030, LocalDate.of(2030, 2, 3));
    }

    public LocalDate getSpringFestivalDate(int year) {
        return SPRING_FESTIVAL_DATES.getOrDefault(year, 
                LocalDate.of(year, 2, 1));
    }

    public NaturalYearRange getNaturalYearRange(int year) {
        LocalDate springFestivalCurrent = getSpringFestivalDate(year);
        LocalDate springFestivalNext = getSpringFestivalDate(year + 1);

        LocalDate startDate = springFestivalCurrent.plusDays(11);
        LocalDate endDate = springFestivalNext.plusDays(10);

        return new NaturalYearRange(year, startDate, endDate);
    }

    public int getCurrentNaturalYear() {
        LocalDate today = LocalDate.now();
        int year = today.getYear();

        LocalDate springFestival = getSpringFestivalDate(year);
        if (today.isBefore(springFestival.plusDays(11))) {
            return year - 1;
        }

        LocalDate nextSpringFestival = getSpringFestivalDate(year + 1);
        if (today.isAfter(nextSpringFestival.plusDays(10))) {
            return year + 1;
        }

        return year;
    }

    public static class NaturalYearRange {
        private final int year;
        private final LocalDate startDate;
        private final LocalDate endDate;

        public NaturalYearRange(int year, LocalDate startDate, LocalDate endDate) {
            this.year = year;
            this.startDate = startDate;
            this.endDate = endDate;
        }

        public int getYear() {
            return year;
        }

        public LocalDate getStartDate() {
            return startDate;
        }

        public LocalDate getEndDate() {
            return endDate;
        }

        public String getName() {
            return year + "自然年";
        }
    }
}