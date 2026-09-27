package com.example.labsurplus.Controller;

import com.example.labsurplus.Api.ApiResponse;
import com.example.labsurplus.Model.SurplusOffer;
import com.example.labsurplus.Service.SurplusOfferService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public ResponseEntity<?> addOffer(@RequestBody @Valid SurplusOffer offer, Errors errors) {
        if (errors.hasErrors())
            return ResponseEntity.status(400).body(new ApiResponse(errors.getFieldError().getDefaultMessage()));
        String message = surplusOfferService.addOffer(offer);
        if (!message.equals("success"))
            return ResponseEntity.status(400).body(new ApiResponse(message));
        return ResponseEntity.status(200).body(new ApiResponse("Offer added successfully"));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateOffer(@PathVariable Integer id, @RequestBody @Valid SurplusOffer offer, Errors errors) {
        if (errors.hasErrors())
            return ResponseEntity.status(400).body(new ApiResponse(errors.getFieldError().getDefaultMessage()));
        String message = surplusOfferService.updateOffer(id, offer);
        if (!message.equals("success"))
            return ResponseEntity.status(400).body(new ApiResponse(message));
        return ResponseEntity.status(200).body(new ApiResponse("Offer updated successfully"));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteOffer(@PathVariable Integer id) {
        String message = surplusOfferService.deleteOffer(id);
        if (!message.equals("success"))
            return ResponseEntity.status(400).body(new ApiResponse(message));
        return ResponseEntity.status(200).body(new ApiResponse("Offer deleted successfully"));
    }

    // ---------------- Extra endpoints ----------------

    @GetMapping("/available/{labId}")
    public ResponseEntity<?> availableForLab(@PathVariable Integer labId) {
        List<SurplusOffer> offers = surplusOfferService.availableForLab(labId);
        if (offers == null)
            return ResponseEntity.status(400).body(new ApiResponse("Lab not found"));
        return ResponseEntity.status(200).body(offers);
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
        String message = surplusOfferService.closeOffer(offerId);
        if (!message.equals("success"))
            return ResponseEntity.status(400).body(new ApiResponse(message));
        return ResponseEntity.status(200).body(new ApiResponse("Offer closed. Pending requests on it were rejected"));
    }

    // [جديد]
    @GetMapping("/urgent")
    public ResponseEntity<?> urgent() {
        return ResponseEntity.status(200).body(surplusOfferService.urgentOffers());
    }
}
