package com.example.labsurplus.Controller;

import com.example.labsurplus.Api.ApiResponse;
import com.example.labsurplus.Model.InventoryItem;
import com.example.labsurplus.Service.InventoryItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/item")
@RequiredArgsConstructor
public class InventoryItemController {

    private final InventoryItemService inventoryItemService;

    @GetMapping("/get")
    public ResponseEntity<?> getAllItems() {
        return ResponseEntity.status(200).body(inventoryItemService.getAllItems());
    }

    @PostMapping("/add")
    public ResponseEntity<?> addItem(@RequestBody @Valid InventoryItem item, Errors errors) {
        if (errors.hasErrors())
            return ResponseEntity.status(400).body(new ApiResponse(errors.getFieldError().getDefaultMessage()));
        String message = inventoryItemService.addItem(item);
        if (!message.equals("success"))
            return ResponseEntity.status(400).body(new ApiResponse(message));
        return ResponseEntity.status(200).body(new ApiResponse("Item added successfully"));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateItem(@PathVariable Integer id, @RequestBody @Valid InventoryItem item, Errors errors) {
        if (errors.hasErrors())
            return ResponseEntity.status(400).body(new ApiResponse(errors.getFieldError().getDefaultMessage()));
        String message = inventoryItemService.updateItem(id, item);
        if (!message.equals("success"))
            return ResponseEntity.status(400).body(new ApiResponse(message));
        return ResponseEntity.status(200).body(new ApiResponse("Item updated successfully"));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteItem(@PathVariable Integer id) {
        String message = inventoryItemService.deleteItem(id);
        if (!message.equals("success"))
            return ResponseEntity.status(400).body(new ApiResponse(message));
        return ResponseEntity.status(200).body(new ApiResponse("Item deleted successfully"));
    }

    // ---------------- Extra endpoints ----------------

    @PutMapping("/consume/{itemId}/{amount}")
    public ResponseEntity<?> consume(@PathVariable Integer itemId, @PathVariable Integer amount) {
        String message = inventoryItemService.consume(itemId, amount);
        if (!message.equals("success"))
            return ResponseEntity.status(400).body(new ApiResponse(message));
        return ResponseEntity.status(200).body(new ApiResponse("Consumption recorded successfully"));
    }

    @GetMapping("/nearExpiry/{labId}")
    public ResponseEntity<?> nearExpiry(@PathVariable Integer labId) {
        List<InventoryItem> items = inventoryItemService.nearExpiry(labId);
        if (items == null)
            return ResponseEntity.status(400).body(new ApiResponse("Lab not found"));
        return ResponseEntity.status(200).body(items);
    }

    // [جديد]
    @PostMapping("/notifyNearExpiry/{labId}")
    public ResponseEntity<?> notifyNearExpiry(@PathVariable Integer labId) {
        String message = inventoryItemService.notifyNearExpiry(labId);
        if (!message.equals("success"))
            return ResponseEntity.status(400).body(new ApiResponse(message));
        return ResponseEntity.status(200).body(new ApiResponse("Near-expiry alert emailed to the lab"));
    }
}
