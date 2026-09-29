package com.example.labsurplus.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SurplusOffer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull(message = "Enter the item ID")
    @Column(nullable = false)
    private Integer itemId;

    // تنحسب تلقائيًا من المختبر المالك للصنف، ما يرسلها المستخدم
    private Integer donorLabId;

    @NotNull(message = "Enter the offered quantity")
    @Positive(message = "Offered quantity has to be more than 0")
    @Column(nullable = false)
    private Integer quantity;

    @NotNull(message = "Enter the announcement end date")
    @Future(message = "Announcement end date has to be in the future")
    @Column(nullable = false)
    private LocalDate announcedUntil;

    private Boolean urgent = false;

    // flagged, announced, requested, approved, transferred, closed, expired
    private String status;
}
