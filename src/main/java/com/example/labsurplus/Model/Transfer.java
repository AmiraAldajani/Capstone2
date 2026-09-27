package com.example.labsurplus.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Transfer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull(message = "Enter the offer ID")
    @Column(nullable = false, unique = true)
    private Integer offerId;

    private Integer fromLabId;

    @NotNull(message = "Enter the receiving lab ID")
    @Column(nullable = false)
    private Integer toLabId;

    // لو ما انرسل، يصير internal تلقائيًا
    @Pattern(regexp = "^(internal|external)$", message = "Type must be either internal or external.")
    private String type;

    // هذي الثلاثة تتعبى وقت الاستلام
    private String receivedBy;
    private Double receivedTemperature;
    private LocalDateTime receivedAt;
}
