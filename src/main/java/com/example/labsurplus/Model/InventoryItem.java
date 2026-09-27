package com.example.labsurplus.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull(message = "Enter the lab ID")
    @Column(nullable = false)
    private Integer labId;

    @NotEmpty(message = "Enter the item name")
    @Column(nullable = false)
    private String name;

    @NotEmpty(message = "Enter the category")
    @Column(nullable = false)
    private String category;

    @NotEmpty(message = "Enter the lot number")
    @Column(nullable = false)
    private String lotNumber;

    @NotNull(message = "Enter the quantity")
    @PositiveOrZero(message = "Quantity can't be negative")
    @Column(nullable = false)
    private Integer quantity;

    @NotEmpty(message = "Enter the unit (e.g. mL, box, vial)")
    @Column(nullable = false)
    private String unit;

    @NotNull(message = "Enter the unit price")
    @PositiveOrZero(message = "Price can't be negative")
    @Column(nullable = false)
    private Double unitPrice;

    @NotNull(message = "Enter the expiry date")
    @Column(nullable = false)
    private LocalDate expiryDate;

    @NotEmpty(message = "Enter the storage condition (e.g. -20C, 4C, room temp)")
    @Column(nullable = false)
    private String storageCondition;

    @PastOrPresent(message = "Last consumed date can't be in the future")
    private LocalDate lastConsumedDate;
}
