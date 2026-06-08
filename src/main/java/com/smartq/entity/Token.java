package com.smartq.entity;

import com.smartq.enums.TokenStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tokens")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Token {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "business_id", nullable = false)
    private Business business;

    @ManyToOne
    @JoinColumn(name = "counter_id", nullable = false)
    private Counter counter;

    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    @Column(name = "token_number", nullable = false)
    private Integer tokenNumber;

    @Column(name = "party_size")
    private Integer partySize = 1;

    @Column(name = "special_request")
    private String specialRequest;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TokenStatus status = TokenStatus.WAITING;

    @Column(name = "estimated_wait_mins")
    private Integer estimatedWaitMins;

    @Column(name = "position_in_queue")
    private Integer positionInQueue;

    @Column(name = "notification_sent")
    private Boolean notificationSent = false;

    @Column(name = "issued_at")
    private LocalDateTime issuedAt;

    @Column(name = "called_at")
    private LocalDateTime calledAt;

    @Column(name = "served_at")
    private LocalDateTime servedAt;

    @PrePersist
    public void prePersist() {
        this.issuedAt = LocalDateTime.now();
    }
}