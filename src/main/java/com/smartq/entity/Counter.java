package com.smartq.entity;

import com.smartq.enums.CounterType;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "counters")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Counter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "business_id", nullable = false)
    private Business business;

    @Column(name = "counter_name", nullable = false)
    private String counterName;

    @Enumerated(EnumType.STRING)
    @Column(name = "counter_type")
    private CounterType counterType;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "current_token")
    private Integer currentToken = 0;

    @Column(name = "staff_name")
    private String staffName;

    @Column(name = "tokens_served_today")
    private Integer tokensServedToday = 0;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}