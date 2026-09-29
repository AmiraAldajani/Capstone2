package com.example.labsurplus.Controller;

import com.example.labsurplus.Api.ApiResponse;
import com.example.labsurplus.Model.SurplusOffer;
import com.example.labsurplus.Service.SurplusOfferService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/offer")
@RequiredArgsConstructor
public class SurplusOfferController {

    private final SurplusOfferService surplusOfferService;

    @GetMapping("/get")
    public ResponseEntity<?> getAllOffers() {
        return ResponseEntity.status(200).body(surplusOfferService.getAllOffers());
    }

    @PostMapping("/add")
    public ResponseEntity<?> addOffer(@RequestBody @Valid SurplusOffer offer) {
        surplusOfferService.addOffer(offer);
        return ResponseEntity.status(200).body(new ApiResponse("Offer added successfully"));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateOffer(@PathVariable Integer id, @RequestBody @Valid SurplusOffer offer) {
        surplusOfferService.updateOffer(id, offer);
        return ResponseEntity.status(200).body(new ApiResponse("Offer updated successfully"));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteOffer(@PathVariable Integer id) {
        surplusOfferService.deleteOffer(id);
        return ResponseEntity.status(200).body(new ApiResponse("Offer deleted successfully"));
    }

    @GetMapping("/available/{labId}")
    public ResponseEntity<?> availableForLab(@PathVariable Integer labId) {
        return ResponseEntity.status(200).body(surplusOfferService.availableForLab(labId));
    }

    @GetMapping("/byCategory/{category}")
    public ResponseEntity<?> byCategory(@PathVariable String category) {
        return ResponseEntity.status(200).body(surplusOfferService.byCategory(category));
    }

    @PutMapping("/expireOld")
    public ResponseEntity<?> expireOld() {
        int count = surplusOfferService.expireOld();
        return ResponseEntity.status(200).body(new ApiResponse(count + " offer(s) marked as expired"));
    }

    @GetMapping("/open")
    public ResponseEntity<?> openOffers() {
        return ResponseEntity.status(200).body(surplusOfferService.openOffers());
    }

    @PutMapping("/close/{offerId}")
    public ResponseEntity<?> close(@PathVariable Integer offerId) {
        surplusOfferService.closeOffer(offerId);
        return ResponseEntity.status(200).body(new ApiResponse("Offer closed. Pending requests on it were rejected"));
    }

    @GetMapping("/urgent")
    public ResponseEntity<?> urgent() {
        return ResponseEntity.status(200).body(surplusOfferService.urgentOffers());
    }
}