package com.example.labsurplus.Controller;

import com.example.labsurplus.Api.ApiResponse;
import com.example.labsurplus.DTO.RequestSummary;
import com.example.labsurplus.Model.SurplusRequest;
import com.example.labsurplus.Service.SurplusRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/request")
@RequiredArgsConstructor
public class SurplusRequestController {

    private final SurplusRequestService surplusRequestService;

    @GetMapping("/get")
    public ResponseEntity<?> getAllRequests() {
        return ResponseEntity.status(200).body(surplusRequestService.getAllRequests());
    }

    @PostMapping("/add")
    public ResponseEntity<?> addRequest(@RequestBody @Valid SurplusRequest request, Errors errors) {
        if (errors.hasErrors())
            return ResponseEntity.status(400).body(new ApiResponse(errors.getFieldError().getDefaultMessage()));
        String message = surplusRequestService.addRequest(request);
        if (!message.equals("success"))
            return ResponseEntity.status(400).body(new ApiResponse(message));
        return ResponseEntity.status(200).body(new ApiResponse("Request added successfully"));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateRequest(@PathVariable Integer id, @RequestBody @Valid SurplusRequest request, Errors errors) {
        if (errors.hasErrors())
            return ResponseEntity.status(400).body(new ApiResponse(errors.getFieldError().getDefaultMessage()));
        String message = surplusRequestService.updateRequest(id, request);
        if (!message.equals("success"))
            return ResponseEntity.status(400).body(new ApiResponse(message));
        return ResponseEntity.status(200).body(new ApiResponse("Request updated successfully"));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteRequest(@PathVariable Integer id) {
        String message = surplusRequestService.deleteRequest(id);
        if (!message.equals("success"))
            return ResponseEntity.status(400).body(new ApiResponse(message));
        return ResponseEntity.status(200).body(new ApiResponse("Request deleted successfully"));
    }

    // ---------------- Extra endpoints ----------------

    @GetMapping("/byOffer/{offerId}")
    public ResponseEntity<?> byOffer(@PathVariable Integer offerId) {
        List<SurplusRequest> requests = surplusRequestService.byOffer(offerId);
        if (requests == null)
            return ResponseEntity.status(400).body(new ApiResponse("Offer not found"));
        return ResponseEntity.status(200).body(requests);
    }

    @GetMapping("/summary/{offerId}")
    public ResponseEntity<?> summary(@PathVariable Integer offerId) {
        RequestSummary summary = surplusRequestService.summary(offerId);
        if (summary == null)
            return ResponseEntity.status(400).body(new ApiResponse("Offer not found"));
        return ResponseEntity.status(200).body(summary);
    }

    @PutMapping("/approve/{requestId}")
    public ResponseEntity<?> approve(@PathVariable Integer requestId) {
        String message = surplusRequestService.approve(requestId);
        if (!message.equals("success"))
            return ResponseEntity.status(400).body(new ApiResponse(message));
        return ResponseEntity.status(200).body(new ApiResponse("Request approved. Other requests on this offer were rejected"));
    }

    @PutMapping("/reject/{requestId}")
    public ResponseEntity<?> reject(@PathVariable Integer requestId) {
        String message = surplusRequestService.reject(requestId);
        if (!message.equals("success"))
            return ResponseEntity.status(400).body(new ApiResponse(message));
        return ResponseEntity.status(200).body(new ApiResponse("Request rejected"));
    }

    // [جديد]
    @GetMapping("/byLab/{labId}")
    public ResponseEntity<?> byLab(@PathVariable Integer labId) {
        List<SurplusRequest> requests = surplusRequestService.byLab(labId);
        if (requests == null)
            return ResponseEntity.status(400).body(new ApiResponse("Lab not found"));
        return ResponseEntity.status(200).body(requests);
    }
}
