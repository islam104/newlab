package com.recruting.demo.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "interviews", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"interviewer", "scheduled_time"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Interview {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull(message = "Заявка не может быть null")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", nullable = false)
    private Application application;
    
    @NotBlank(message = "Имя интервьюера не может быть пустым")
    @Column(nullable = false)
    private String interviewer;
    
    @NotNull(message = "Время интервью не может быть null")
    @Column(name = "scheduled_time", nullable = false)
    private LocalDateTime scheduledTime;
    
    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes = 60;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InterviewStatus status = InterviewStatus.SCHEDULED;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InterviewType type = InterviewType.TECHNICAL;
    
    @Column(columnDefinition = "TEXT")
    private String notes;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public enum InterviewStatus {
        SCHEDULED, COMPLETED, CANCELLED, NO_SHOW
    }
    
    public enum InterviewType {
        TECHNICAL, HR, FINAL, PHONE
    }
}
