package com.smartq.entity;

import com.smartq.enums.BusinessType;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "businesses")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Business {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "business_type", nullable = false)
    private BusinessType businessType;

    private String address;
    private String city;

    @Column(name = "is_queue_open")
    private Boolean isQueueOpen = false;

    @Column(name = "max_queue_size")
    private Integer maxQueueSize = 100;

    @Column(name = "avg_service_mins")
    private Integer avgServiceMins = 10;

    @Column(name = "avg_rating")
    private Double avgRating = 0.0;

    @Column(name = "total_reviews")
    private Integer totalReviews = 0;

    @Column(name = "qr_code_url")
    private String qrCodeUrl;

    @Column(name = "called_message")
    private String calledMessage = "Your turn has arrived. Please proceed.";

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}