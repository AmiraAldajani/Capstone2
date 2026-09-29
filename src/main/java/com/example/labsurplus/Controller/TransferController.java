package com.example.labsurplus.Controller;

import com.example.labsurplus.Api.ApiResponse;
import com.example.labsurplus.Model.Transfer;
import com.example.labsurplus.Service.TransferService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/transfer")
@RequiredArgsConstructor
public class TransferController {

    private final TransferService transferService;

    @GetMapping("/get")
    public ResponseEntity<?> getAllTransfers() {
        return ResponseEntity.status(200).body(transferService.getAllTransfers());
    }

    @PostMapping("/add")
    public ResponseEntity<?> addTransfer(@RequestBody @Valid Transfer transfer) {
        transferService.addTransfer(transfer);
        return ResponseEntity.status(200).body(new ApiResponse("Transfer added successfully"));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateTransfer(@PathVariable Integer id, @RequestBody @Valid Transfer transfer) {
        transferService.updateTransfer(id, transfer);
        return ResponseEntity.status(200).body(new ApiResponse("Transfer updated successfully"));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteTransfer(@PathVariable Integer id) {
        transferService.deleteTransfer(id);
        return ResponseEntity.status(200).body(new ApiResponse("Transfer deleted successfully"));
    }
    @PutMapping("/receive/{transferId}/{receivedBy}/{temperature}")
    public ResponseEntity<?> receive(@PathVariable Integer transferId, @PathVariable String receivedBy, @PathVariable Double temperature) {
        transferService.receive(transferId, receivedBy, temperature);
        return ResponseEntity.status(200).body(new ApiResponse("Received successfully. Inventory moved to the receiving lab"));
    }

    @GetMapping("/pending/{labId}")
    public ResponseEntity<?> pending(@PathVariable Integer labId) {
        return ResponseEntity.status(200).body(transferService.pendingForLab(labId));
    }
}