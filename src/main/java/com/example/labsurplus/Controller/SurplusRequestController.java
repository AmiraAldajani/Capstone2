package com.example.labsurplus.Controller;

import com.example.labsurplus.Api.ApiResponse;
import com.example.labsurplus.Model.SurplusRequest;
import com.example.labsurplus.Service.SurplusRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<?> addRequest(@RequestBody @Valid SurplusRequest request) {
        surplusRequestService.addRequest(request);
        return ResponseEntity.status(200).body(new ApiResponse("Request added successfully"));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateRequest(@PathVariable Integer id, @RequestBody @Valid SurplusRequest request) {
        surplusRequestService.updateRequest(id, request);
        return ResponseEntity.status(200).body(new ApiResponse("Request updated successfully"));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteRequest(@PathVariable Integer id) {
        surplusRequestService.deleteRequest(id);
        return ResponseEntity.status(200).body(new ApiResponse("Request deleted successfully"));
    }

    @GetMapping("/byOffer/{offerId}")
    public ResponseEntity<?> byOffer(@PathVariable Integer offerId) {
        return ResponseEntity.status(200).body(surplusRequestService.byOffer(offerId));
    }

    @GetMapping("/summary/{offerId}")
    public ResponseEntity<?> summary(@PathVariable Integer offerId) {
        return ResponseEntity.status(200).body(surplusRequestService.summary(offerId));
    }

    @PutMapping("/approve/{requestId}")
    public ResponseEntity<?> approve(@PathVariable Integer requestId) {
        surplusRequestService.approve(requestId);
        return ResponseEntity.status(200).body(new ApiResponse("Request approved. Other requests on this offer were rejected"));
    }

    @PutMapping("/reject/{requestId}")
    public ResponseEntity<?> reject(@PathVariable Integer requestId) {
        surplusRequestService.reject(requestId);
        return ResponseEntity.status(200).body(new ApiResponse("Request rejected"));
    }

    @GetMapping("/byLab/{labId}")
    public ResponseEntity<?> byLab(@PathVariable Integer labId) {
        return ResponseEntity.status(200).body(surplusRequestService.byLab(labId));
    }
}