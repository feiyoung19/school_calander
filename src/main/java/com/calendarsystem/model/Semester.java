package com.calendarsystem.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Data
public class Semester {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String term;
    private LocalDate startDate;
    private LocalDate endDate;
    private int weekCount;
    
    @ManyToOne
    @JoinColumn(name = "academic_year_id")
    private AcademicYear academicYear;
}
