package com.example.labsurplus.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SurplusRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull(message = "Enter the offer ID")
    @Column(nullable = false)
    private Integer offerId;

    @NotNull(message = "Enter the requesting lab ID")
    @Column(nullable = false)
    private Integer requestingLabId;

    @NotNull(message = "Enter the requested quantity")
    @Positive(message = "Requested quantity has to be more than 0")
    @Column(nullable = false)
    private Integer quantity;

    @NotEmpty(message = "Explain why the lab needs this item")
    @Column(nullable = false)
    private String justification;
    // pending, approved, rejected
    private String status;
}
