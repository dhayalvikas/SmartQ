package com.smartq.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "feedback")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Feedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "token_id", nullable = false)
    private Token token;

    @ManyToOne
    @JoinColumn(name = "business_id", nullable = false)
    private Business business;

    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    @Column(name = "wait_rating")
    private Integer waitRating;

    @Column(name = "service_rating")
    private Integer serviceRating;

    @Column(name = "overall_rating")
    private Integer overallRating;

    @Column(columnDefinition = "TEXT")
    private String comment;

    @Column(name = "would_return")
    private Boolean wouldReturn;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}