package com.calendarsystem.service;

import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.*;
import net.fortuna.ical4j.util.RandomUidGenerator;
import net.fortuna.ical4j.util.UidGenerator;
import org.springframework.stereotype.Service;

import java.io.StringWriter;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

@Service
public class ICalService {

    public String generateCalendarIcs(Long yearId, CalendarService calendarService) throws Exception {
        List<Map<String, Object>> events = calendarService.getCalendarEvents(yearId);
        Calendar icsCalendar = new Calendar();
        icsCalendar.getProperties().add(new ProdId("-//University Calendar//iCal4j 1.0//EN"));
        icsCalendar.getProperties().add(Version.VERSION_2_0);
        icsCalendar.getProperties().add(CalScale.GREGORIAN);

        UidGenerator uidGenerator = new RandomUidGenerator();

        for (Map<String, Object> ev : events) {
            if ("background".equals(ev.get("display"))) continue;

            LocalDate startDate = LocalDate.parse((String) ev.get("start"));
            LocalDate endDate = null;

            Object endValue = ev.get("end");
            if (endValue != null && !endValue.toString().isEmpty()) {
                endDate = LocalDate.parse(endValue.toString());
            }

            if (endDate == null) {
                endDate = startDate;
            }

            VEvent vEvent = new VEvent(
                    new Date(java.util.Date.from(startDate.atStartOfDay(ZoneId.systemDefault()).toInstant())),
                    new Date(java.util.Date.from(endDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant())),
                    (String) ev.get("title")
            );
            vEvent.getProperties().add(uidGenerator.generateUid());

            String description = (String) ev.get("description");
            if (description != null && !description.isEmpty()) {
                vEvent.getProperties().add(new Description(description));
            }

            icsCalendar.getComponents().add(vEvent);
        }

        StringWriter writer = new StringWriter();
        CalendarOutputter outputter = new CalendarOutputter();
        outputter.output(icsCalendar, writer);
        return writer.toString();
    }
}
