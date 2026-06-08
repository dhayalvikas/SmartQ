package com.smartq.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "queue_sessions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QueueSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "business_id", nullable = false)
    private Business business;

    private LocalDate date;

    @Column(name = "opened_at")
    private LocalDateTime openedAt;

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    @Column(name = "total_served")
    private Integer totalServed = 0;

    @Column(name = "total_abandoned")
    private Integer totalAbandoned = 0;

    @Column(name = "avg_wait_mins")
    private Integer avgWaitMins = 0;

    @Column(name = "peak_hour")
    private Integer peakHour;

    @Column(name = "max_queue_reached")
    private Integer maxQueueReached = 0;

    @Column(name = "avg_party_size")
    private Double avgPartySize = 0.0;

    @Column(name = "revenue_estimate")
    private Double revenueEstimate = 0.0;
}