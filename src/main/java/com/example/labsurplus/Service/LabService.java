package com.example.labsurplus.Service;

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


    public String addLab(Lab lab) {
        if (labRepository.existsByName(lab.getName()))
            return "A lab with this name already exists";
        lab.setId(null); // [جديد] لو انرسل id ما يكتب فوق مختبر موجود
        labRepository.save(lab);
        return "success";
    }

    public String updateLab(Integer id, Lab lab) {
        Lab old = labRepository.findLabById(id);
        if (old == null)
            return "Lab not found";
        if (!old.getName().equals(lab.getName()) && labRepository.existsByName(lab.getName()))
            return "A lab with this name already exists";
        old.setName(lab.getName());
        old.setCenterId(lab.getCenterId());
        old.setHeadName(lab.getHeadName());
        old.setEmail(lab.getEmail());
        labRepository.save(old);
        return "success";
    }

    public String deleteLab(Integer id) {
        Lab lab = labRepository.findLabById(id);
        if (lab == null)
            return "Lab not found";
        if (inventoryItemRepository.existsByLabId(id))
            return "Can't delete a lab that still has inventory items";
        if (surplusRequestRepository.existsByRequestingLabId(id) || transferRepository.existsByToLabId(id))
            return "Can't delete a lab that has requests or transfers";
        labRepository.delete(lab);
        return "success";
    }

    // ---------------- Extra endpoints ----------------

    // مجموع قيمة المواد اللي استلمها المختبر من الفائض بدل ما يشتريها
    public Double savedValue(Integer labId) {
        if (labRepository.findLabById(labId) == null)
            return null;
        double total = 0;
        for (Transfer t : transferRepository.findAllByToLabIdAndReceivedAtIsNotNull(labId))
            total = total + transferValue(t);
        return total;
    }

    // [جديد] مجموع قيمة المواد اللي أعطاها المختبر لغيره بدل ما تنتهي عنده
    public Double donatedValue(Integer labId) {
        if (labRepository.findLabById(labId) == null)
            return null;
        double total = 0;
        for (Transfer t : transferRepository.findAllByFromLabIdAndReceivedAtIsNotNull(labId))
            total = total + transferValue(t);
        return total;
    }

    // قيمة الأصناف المنتهية اللي لسا بالمخزون
    public Double wastedValue(Integer labId) {
        if (labRepository.findLabById(labId) == null)
            return null;
        LocalDate today = LocalDate.now();
        double total = 0;
        for (InventoryItem item : inventoryItemRepository.findAllByLabId(labId))
            // [تعديل] كانت isBefore: الصنف اللي ينتهي اليوم ما كان ينحسب لا هنا ولا في nearExpiry
            if (!item.getExpiryDate().isAfter(today))
                total = total + item.getUnitPrice() * item.getQuantity();
        return total;
    }

    // [جديد] قيمة تحويل واحد = سعر الوحدة × الكمية المعتمدة (تستخدمها savedValue و donatedValue)
    private double transferValue(Transfer t) {
        SurplusOffer offer = surplusOfferRepository.findSurplusOfferById(t.getOfferId());
        InventoryItem item = inventoryItemRepository.findInventoryItemById(offer.getItemId());
        SurplusRequest request = surplusRequestRepository.findSurplusRequestByOfferIdAndStatus(offer.getId(), "approved");
        return item.getUnitPrice() * request.getQuantity();
    }
}
