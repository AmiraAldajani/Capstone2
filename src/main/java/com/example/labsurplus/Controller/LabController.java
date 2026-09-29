package com.example.labsurplus.Controller;

import com.example.labsurplus.Api.ApiResponse;
import com.example.labsurplus.Model.Lab;
import com.example.labsurplus.Service.LabService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/lab")
@RequiredArgsConstructor
public class LabController {

    private final LabService labService;

    @GetMapping("/get")
    public ResponseEntity<?> getAllLabs() {
        return ResponseEntity.status(200).body(labService.getAllLabs());
    }
    @PostMapping("/add")
    public ResponseEntity<?> addLab(@RequestBody @Valid Lab lab) {
        labService.addLab(lab);
        return ResponseEntity.status(200).body(new ApiResponse("Lab added successfully"));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateLab(@PathVariable Integer id, @RequestBody @Valid Lab lab) {
        labService.updateLab(id, lab);
        return ResponseEntity.status(200).body(new ApiResponse("Lab updated successfully"));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteLab(@PathVariable Integer id) {
        labService.deleteLab(id);
        return ResponseEntity.status(200).body(new ApiResponse("Lab deleted successfully"));
    }

    @GetMapping("/savedValue/{labId}")
    public ResponseEntity<?> savedValue(@PathVariable Integer labId) {
        Double total = labService.savedValue(labId);
        return ResponseEntity.status(200).body(new ApiResponse("Total value received from surplus: " + total + " SAR"));
    }
    @GetMapping("/wastedValue/{labId}")
    public ResponseEntity<?> wastedValue(@PathVariable Integer labId) {
        Double total = labService.wastedValue(labId);
        return ResponseEntity.status(200).body(new ApiResponse("Total value of expired items still in stock: " + total + " SAR"));
    }

    @GetMapping("/donatedValue/{labId}")
    public ResponseEntity<?> donatedValue(@PathVariable Integer labId) {
        Double total = labService.donatedValue(labId);
        return ResponseEntity.status(200).body(new ApiResponse("Total value donated to other labs: " + total + " SAR"));
    }
}
