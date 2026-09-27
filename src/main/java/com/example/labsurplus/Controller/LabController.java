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
    public ResponseEntity<?> addLab(@RequestBody @Valid Lab lab, Errors errors) {
        if (errors.hasErrors())
            return ResponseEntity.status(400).body(new ApiResponse(errors.getFieldError().getDefaultMessage()));
        String message = labService.addLab(lab);
        if (!message.equals("success"))
            return ResponseEntity.status(400).body(new ApiResponse(message));
        return ResponseEntity.status(200).body(new ApiResponse("Lab added successfully"));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateLab(@PathVariable Integer id, @RequestBody @Valid Lab lab, Errors errors) {
        if (errors.hasErrors())
            return ResponseEntity.status(400).body(new ApiResponse(errors.getFieldError().getDefaultMessage()));
        String message = labService.updateLab(id, lab);
        if (!message.equals("success"))
            return ResponseEntity.status(400).body(new ApiResponse(message));
        return ResponseEntity.status(200).body(new ApiResponse("Lab updated successfully"));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteLab(@PathVariable Integer id) {
        String message = labService.deleteLab(id);
        if (!message.equals("success"))
            return ResponseEntity.status(400).body(new ApiResponse(message));
        return ResponseEntity.status(200).body(new ApiResponse("Lab deleted successfully"));
    }

    // ---------------- Extra endpoints ----------------

    @GetMapping("/savedValue/{labId}")
    public ResponseEntity<?> savedValue(@PathVariable Integer labId) {
        Double total = labService.savedValue(labId);
        if (total == null)
            return ResponseEntity.status(400).body(new ApiResponse("Lab not found"));
        return ResponseEntity.status(200).body(new ApiResponse("Total value received from surplus: " + total + " SAR"));
    }
    @GetMapping("/wastedValue/{labId}")
    public ResponseEntity<?> wastedValue(@PathVariable Integer labId) {
        Double total = labService.wastedValue(labId);
        if (total == null)
            return ResponseEntity.status(400).body(new ApiResponse("Lab not found"));
        return ResponseEntity.status(200).body(new ApiResponse("Total value of expired items still in stock: " + total + " SAR"));
    }

    // [جديد]
    @GetMapping("/donatedValue/{labId}")
    public ResponseEntity<?> donatedValue(@PathVariable Integer labId) {
        Double total = labService.donatedValue(labId);
        if (total == null)
            return ResponseEntity.status(400).body(new ApiResponse("Lab not found"));
        return ResponseEntity.status(200).body(new ApiResponse("Total value donated to other labs: " + total + " SAR"));
    }
}
