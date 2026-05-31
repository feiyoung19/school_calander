package com.calendarsystem.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Data
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String title;
    
    @Enumerated(EnumType.STRING)
    private EventType eventType;
    
    private LocalDate startDate;
    private LocalDate endDate;
    
    @ManyToOne
    @JoinColumn(name = "semester_id")
    private Semester semester;
    
    private Integer weekNumber;
    private int priority;
    
    @Enumerated(EnumType.STRING)
    private Status status = Status.DRAFT;
    
    private String description;

    public enum EventType {
        SPORTS, EXAM, CEREMONY, OTHER
    }

    public enum Status {
        DRAFT, CONFIRMED
    }
}
