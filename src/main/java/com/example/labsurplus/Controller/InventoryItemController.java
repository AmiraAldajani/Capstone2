package com.example.labsurplus.Controller;

import com.example.labsurplus.Api.ApiResponse;
import com.example.labsurplus.Model.InventoryItem;
import com.example.labsurplus.Service.InventoryItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<?> addItem(@RequestBody @Valid InventoryItem item) {
        inventoryItemService.addItem(item);
        return ResponseEntity.status(200).body(new ApiResponse("Item added successfully"));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateItem(@PathVariable Integer id, @RequestBody @Valid InventoryItem item){
        inventoryItemService.updateItem(id, item);
        return ResponseEntity.status(200).body(new ApiResponse("Item updated successfully"));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteItem(@PathVariable Integer id) {
        inventoryItemService.deleteItem(id);
        return ResponseEntity.status(200).body(new ApiResponse("Item deleted successfully"));
    }

    @PutMapping("/consume/{itemId}/{amount}")
    public ResponseEntity<?> consume(@PathVariable Integer itemId, @PathVariable Integer amount) {
        inventoryItemService.consume(itemId, amount);
        return ResponseEntity.status(200).body(new ApiResponse("Consumption recorded successfully"));
    }

    @GetMapping("/nearExpiry/{labId}")
    public ResponseEntity<?> nearExpiry(@PathVariable Integer labId) {
        return ResponseEntity.status(200).body(inventoryItemService.nearExpiry(labId));
    }

    @PostMapping("/notifyNearExpiry/{labId}")
    public ResponseEntity<?> notifyNearExpiry(@PathVariable Integer labId) {
        inventoryItemService.notifyNearExpiry(labId);
        return ResponseEntity.status(200).body(new ApiResponse("Near-expiry alert emailed to the lab"));
    }
}
