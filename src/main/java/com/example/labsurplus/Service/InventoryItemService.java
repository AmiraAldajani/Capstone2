package com.example.labsurplus.Service;

import com.example.labsurplus.Api.ApiException;
import com.example.labsurplus.Model.InventoryItem;
import com.example.labsurplus.Model.Lab;
import com.example.labsurplus.Repository.InventoryItemRepository;
import com.example.labsurplus.Repository.LabRepository;
import com.example.labsurplus.Repository.SurplusOfferRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryItemService {

    private final InventoryItemRepository inventoryItemRepository;
    private final LabRepository labRepository;
    private final SurplusOfferRepository surplusOfferRepository;
    private final SurplusOfferService surplusOfferService; // [جديد] عشان نعرف الكمية المحجوزة
    private final EmailService emailService;               // [جديد]

    public List<InventoryItem> getAllItems() {
        return inventoryItemRepository.findAll();
    }

    public void addItem(InventoryItem item) {
        if (labRepository.findLabById(item.getLabId()) == null)
            throw new ApiException("Lab not found");
        item.setId(null); // لو انرسل id بالـ body ما يكتب فوق صنف موجود
        inventoryItemRepository.save(item);
    }

    public void updateItem(Integer id, InventoryItem item) {
        InventoryItem old = inventoryItemRepository.findInventoryItemById(id);
        if (old == null)
            throw new ApiException("Item not found");
        if (labRepository.findLabById(item.getLabId()) == null)
            throw new ApiException("Lab not found");
        if (!old.getLabId().equals(item.getLabId()) && surplusOfferRepository.existsByItemId(id))
            throw new ApiException("Can't change the lab of an item that has surplus offers");
        if (item.getQuantity() < surplusOfferService.reservedQuantity(id))
            throw new ApiException("Quantity can't be less than what is reserved in open offers");
        old.setLabId(item.getLabId());
        old.setName(item.getName());
        old.setCategory(item.getCategory());
        old.setLotNumber(item.getLotNumber());
        old.setQuantity(item.getQuantity());
        old.setUnit(item.getUnit());
        old.setUnitPrice(item.getUnitPrice());
        old.setExpiryDate(item.getExpiryDate());
        old.setStorageCondition(item.getStorageCondition());
        old.setLastConsumedDate(item.getLastConsumedDate());
        inventoryItemRepository.save(old);
    }

    public void deleteItem(Integer id) {
        InventoryItem item = inventoryItemRepository.findInventoryItemById(id);
        if (item == null)
            throw new ApiException("Item not found");
        if (surplusOfferRepository.existsByItemId(id))
            throw new ApiException("Can't delete an item that has surplus offers");
        inventoryItemRepository.delete(item);
    }


    public void consume(Integer itemId, Integer amount) {
        InventoryItem item = inventoryItemRepository.findInventoryItemById(itemId);
        if (item == null)
            throw new ApiException("Item not found");
        if (amount < 1)
            throw new ApiException("Amount has to be 1 or more");
        if (amount > item.getQuantity() - surplusOfferService.reservedQuantity(itemId))
            throw new ApiException("Amount is more than the available quantity (part of it is reserved in surplus offers)");
        item.setQuantity(item.getQuantity() - amount);
        item.setLastConsumedDate(LocalDate.now());
        inventoryItemRepository.save(item);
    }

    public List<InventoryItem> nearExpiry(Integer labId) {
        if (labRepository.findLabById(labId) == null)
            throw new ApiException("Lab ID not found");
        LocalDate today = LocalDate.now();
        List<InventoryItem> result = new ArrayList<>();
        for (InventoryItem item : inventoryItemRepository.findAllByLabId(labId)) {
            if (item.getQuantity() < 1)
                continue;
            boolean notExpiredYet = item.getExpiryDate().isAfter(today);
            boolean expiresSoon = item.getExpiryDate().isBefore(today.plusMonths(6));
            boolean unused = item.getLastConsumedDate() == null
                    || item.getLastConsumedDate().isBefore(today.minusMonths(3));
            if (notExpiredYet && expiresSoon && unused)
                result.add(item);
        }
        return result;
    }

    public void notifyNearExpiry(Integer labId) {
        Lab lab = labRepository.findLabById(labId);
        if (lab == null)
            throw new ApiException("Lab not found");
        if (lab.getEmail() == null || lab.getEmail().isBlank())
            throw new ApiException("This lab has no email");
        List<InventoryItem> items = nearExpiry(labId);
        if (items.isEmpty())
            throw new ApiException("No items close to expiry, nothing to send");
        StringBuilder text = new StringBuilder();
        text.append("These items expire within 6 months and haven't been used in the last 3 months.\n");
        text.append("Consider offering them as surplus so another lab can use them:\n\n");
        for (InventoryItem item : items)
            text.append("- ").append(item.getName())
                    .append(" | lot ").append(item.getLotNumber())
                    .append(" | ").append(item.getQuantity()).append(" ").append(item.getUnit())
                    .append(" | expires ").append(item.getExpiryDate()).append("\n");
        emailService.notifyLab(lab, "Items close to expiry in " + lab.getName(), text.toString());
    }
}
