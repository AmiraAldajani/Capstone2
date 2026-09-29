package com.example.labsurplus.Service;

import com.example.labsurplus.Api.ApiException;
import com.example.labsurplus.Model.InventoryItem;
import com.example.labsurplus.Model.Lab;
import com.example.labsurplus.Model.SurplusOffer;
import com.example.labsurplus.Model.SurplusRequest;
import com.example.labsurplus.Model.Transfer;
import com.example.labsurplus.Repository.InventoryItemRepository;
import com.example.labsurplus.Repository.LabRepository;
import com.example.labsurplus.Repository.SurplusOfferRepository;
import com.example.labsurplus.Repository.SurplusRequestRepository;
import com.example.labsurplus.Repository.TransferRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransferService {

    private final TransferRepository transferRepository;
    private final SurplusOfferRepository surplusOfferRepository;
    private final SurplusRequestRepository surplusRequestRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final LabRepository labRepository;
    private final EmailService emailService;

    public List<Transfer> getAllTransfers() {
        return transferRepository.findAll();
    }

    public void addTransfer(Transfer transfer) {
        SurplusOffer offer = surplusOfferRepository.findSurplusOfferById(transfer.getOfferId());
        if (offer == null)
            throw new ApiException("Offer not found");
        if (!offer.getStatus().equals("approved"))
            throw new ApiException("The offer has to be approved before creating a transfer");
        if (transferRepository.existsByOfferId(transfer.getOfferId()))
            throw new ApiException("This offer already has a transfer");
        SurplusRequest approved = surplusRequestRepository.findSurplusRequestByOfferIdAndStatus(offer.getId(), "approved");
        if (!transfer.getToLabId().equals(approved.getRequestingLabId()))
            throw new ApiException("Receiving lab has to be the lab whose request was approved");
        transfer.setId(null);
        transfer.setFromLabId(offer.getDonorLabId());
        if (transfer.getType() == null)
            transfer.setType("internal");
        transfer.setReceivedBy(null);
        transfer.setReceivedTemperature(null);
        transfer.setReceivedAt(null);
        transferRepository.save(transfer);

        Lab donor = labRepository.findLabById(offer.getDonorLabId());
        emailService.notifyLab(labRepository.findLabById(transfer.getToLabId()),
                "Surplus transfer #" + transfer.getId() + " is on its way",
                "Transfer #" + transfer.getId() + " for offer #" + offer.getId() + " is coming from "
                        + (donor != null ? donor.getName() : "the donor lab")
                        + ". Please record the temperature when you receive it.");
    }

    public void updateTransfer(Integer id, Transfer transfer) {
        Transfer old = transferRepository.findTransferById(id);
        if (old == null)
            throw new ApiException("Transfer not found");
        if (old.getReceivedAt() != null)
            throw new ApiException("Can't update a transfer that was already received");
        if (transfer.getType() != null)
            old.setType(transfer.getType());
        transferRepository.save(old);
    }

    public void deleteTransfer(Integer id) {
        Transfer transfer = transferRepository.findTransferById(id);
        if (transfer == null)
            throw new ApiException("Transfer not found");
        if (transfer.getReceivedAt() != null)
            throw new ApiException("Can't delete a transfer that was already received");
        transferRepository.delete(transfer);
    }

    @Transactional
    public void receive(Integer transferId, String receivedBy, Double temperature) {
        Transfer transfer = transferRepository.findTransferById(transferId);
        if (transfer == null)
            throw new ApiException("Transfer not found");
        if (transfer.getReceivedAt() != null)
            throw new ApiException("This transfer was already received");

        SurplusOffer offer = surplusOfferRepository.findSurplusOfferById(transfer.getOfferId());
        SurplusRequest approved = surplusRequestRepository.findSurplusRequestByOfferIdAndStatus(offer.getId(), "approved");
        InventoryItem donorItem = inventoryItemRepository.findInventoryItemById(offer.getItemId());
        int quantity = approved.getQuantity();

        if (donorItem.getQuantity() < quantity)
            throw new ApiException("Donor lab doesn't have enough quantity anymore");

        donorItem.setQuantity(donorItem.getQuantity() - quantity);
        inventoryItemRepository.save(donorItem);

        InventoryItem receivedItem = new InventoryItem(null, transfer.getToLabId(), donorItem.getName(),
                donorItem.getCategory(), donorItem.getLotNumber(), quantity, donorItem.getUnit(),
                donorItem.getUnitPrice(), donorItem.getExpiryDate(), donorItem.getStorageCondition(), null);
        inventoryItemRepository.save(receivedItem);

        transfer.setReceivedBy(receivedBy);
        transfer.setReceivedTemperature(temperature);
        transfer.setReceivedAt(LocalDateTime.now());
        transferRepository.save(transfer);

        offer.setStatus("transferred");
        surplusOfferRepository.save(offer);

        emailService.notifyLab(labRepository.findLabById(transfer.getFromLabId()),
                "Surplus transfer #" + transferId + " was received",
                "Transfer #" + transferId + " (" + quantity + " " + donorItem.getUnit() + " of " + donorItem.getName()
                        + ") was received by " + receivedBy + " at " + temperature + " C.");
    }

    public List<Transfer> pendingForLab(Integer labId) {
        if (labRepository.findLabById(labId) == null)
            throw new ApiException("Lab not found");
        return transferRepository.findAllByToLabIdAndReceivedAtIsNull(labId);
    }
}