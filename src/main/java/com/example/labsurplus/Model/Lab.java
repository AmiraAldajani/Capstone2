package com.example.labsurplus.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Lab {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotEmpty(message = "Enter the lab name")
    @Size(min = 3, message = "Lab name has to be 3 characters or more")
    @Column(nullable = false, unique = true)
    private String name;

    // اختياري حاليًا، يصير إلزامي لما نضيف كلاس Center
    private Integer centerId;

    @NotEmpty(message = "Enter the lab head's name")
    @Column(nullable = false)
    private String headName;

    @NotEmpty(message = "Enter the lab email")
    @Email(message = "Enter a valid email")
    private String email;
}
