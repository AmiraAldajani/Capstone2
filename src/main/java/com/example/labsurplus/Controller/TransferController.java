package com.example.labsurplus.Controller;

import com.example.labsurplus.Api.ApiResponse;
import com.example.labsurplus.Model.Transfer;
import com.example.labsurplus.Service.TransferService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
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
    public ResponseEntity<?> addTransfer(@RequestBody @Valid Transfer transfer, Errors errors) {
        if (errors.hasErrors())
            return ResponseEntity.status(400).body(new ApiResponse(errors.getFieldError().getDefaultMessage()));
        String message = transferService.addTransfer(transfer);
        if (!message.equals("success"))
            return ResponseEntity.status(400).body(new ApiResponse(message));
        return ResponseEntity.status(200).body(new ApiResponse("Transfer added successfully"));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateTransfer(@PathVariable Integer id, @RequestBody @Valid Transfer transfer, Errors errors) {
        if (errors.hasErrors())
            return ResponseEntity.status(400).body(new ApiResponse(errors.getFieldError().getDefaultMessage()));
        String message = transferService.updateTransfer(id, transfer);
        if (!message.equals("success"))
            return ResponseEntity.status(400).body(new ApiResponse(message));
        return ResponseEntity.status(200).body(new ApiResponse("Transfer updated successfully"));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteTransfer(@PathVariable Integer id) {
        String message = transferService.deleteTransfer(id);
        if (!message.equals("success"))
            return ResponseEntity.status(400).body(new ApiResponse(message));
        return ResponseEntity.status(200).body(new ApiResponse("Transfer deleted successfully"));
    }

    // ---------------- Extra endpoints ----------------

    @PutMapping("/receive/{transferId}/{receivedBy}/{temperature}")
    public ResponseEntity<?> receive(@PathVariable Integer transferId, @PathVariable String receivedBy, @PathVariable Double temperature) {
        String message = transferService.receive(transferId, receivedBy, temperature);
        if (!message.equals("success"))
            return ResponseEntity.status(400).body(new ApiResponse(message));
        return ResponseEntity.status(200).body(new ApiResponse("Received successfully. Inventory moved to the receiving lab"));
    }
}
