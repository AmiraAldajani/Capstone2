package com.example.labsurplus.Service;

import com.example.labsurplus.Api.ApiException;
import com.example.labsurplus.Model.*;
import com.example.labsurplus.Repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LabService {

    private final LabRepository labRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final SurplusOfferRepository surplusOfferRepository;
    private final SurplusRequestRepository surplusRequestRepository;
    private final TransferRepository transferRepository;

    public List<Lab> getAllLabs() {
        return labRepository.findAll();
    }

    public void addLab(Lab lab) {
        if (labRepository.existsByName(lab.getName()))
            throw new ApiException("A lab with this name already exists");
        lab.setId(null);
        labRepository.save(lab);
    }

    public void updateLab(Integer id, Lab lab) {
        Lab old = labRepository.findLabById(id);
        if (old == null)
            throw new ApiException("Lab not found");
        if (!old.getName().equals(lab.getName()) && labRepository.existsByName(lab.getName()))
            throw new ApiException("A lab with this name already exists");
        old.setName(lab.getName());
        old.setCenterId(lab.getCenterId());
        old.setHeadName(lab.getHeadName());
        old.setEmail(lab.getEmail());
        labRepository.save(old);
    }

    public void deleteLab(Integer id) {
        Lab lab = labRepository.findLabById(id);
        if (lab == null)
            throw new ApiException("Lab not found");
        if (inventoryItemRepository.existsByLabId(id))
            throw new ApiException("Can't delete a lab that still has inventory items");
        if (surplusRequestRepository.existsByRequestingLabId(id) || transferRepository.existsByToLabId(id))
            throw new ApiException("Can't delete a lab that has requests or transfers");
        labRepository.delete(lab);
    }

    public Double savedValue(Integer labId) {
        if (labRepository.findLabById(labId) == null)
            throw new ApiException("Lab not found");
        double total = 0;
        for (Transfer t : transferRepository.findAllByToLabIdAndReceivedAtIsNotNull(labId))
            total = total + transferValue(t);
        return total;
    }

    public Double donatedValue(Integer labId) {
        if (labRepository.findLabById(labId) == null)
            throw new ApiException("Lab not found");
        double total = 0;
        for (Transfer t : transferRepository.findAllByFromLabIdAndReceivedAtIsNotNull(labId))
            total = total + transferValue(t);
        return total;
    }

    // قيمة الأصناف المنتهية اللي لسا بالمخزون
    public Double wastedValue(Integer labId) {
        if (labRepository.findLabById(labId) == null)
            throw new ApiException("Lab not found");
        LocalDate today = LocalDate.now();
        double total = 0;
        for (InventoryItem item : inventoryItemRepository.findAllByLabId(labId))

            if (!item.getExpiryDate().isAfter(today))
                total = total + item.getUnitPrice() * item.getQuantity();
        return total;
    }


    private double transferValue(Transfer t) {
        SurplusOffer offer = surplusOfferRepository.findSurplusOfferById(t.getOfferId());
        InventoryItem item = inventoryItemRepository.findInventoryItemById(offer.getItemId());
        SurplusRequest request = surplusRequestRepository.findSurplusRequestByOfferIdAndStatus(offer.getId(), "approved");
        return item.getUnitPrice() * request.getQuantity();
    }
}
