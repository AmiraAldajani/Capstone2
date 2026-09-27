package com.example.labsurplus.Service;

import com.example.labsurplus.Model.InventoryItem;
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

    public List<InventoryItem> getAllItems() {
        return inventoryItemRepository.findAll();
    }

    public String addItem(InventoryItem item) {
        if (labRepository.findLabById(item.getLabId()) == null)
            return "Lab not found";
        inventoryItemRepository.save(item);
        return "success";
    }

    public String updateItem(Integer id, InventoryItem item) {
        InventoryItem old = inventoryItemRepository.findInventoryItemById(id);
        if (old == null)
            return "Item not found";
        if (labRepository.findLabById(item.getLabId()) == null)
            return "Lab not found";
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
        return "success";
    }

    public String deleteItem(Integer id) {
        InventoryItem item = inventoryItemRepository.findInventoryItemById(id);
        if (item == null)
            return "Item not found";
        if (surplusOfferRepository.existsByItemId(id))
            return "Can't delete an item that has surplus offers";
        inventoryItemRepository.delete(item);
        return "success";
    }

    public String consume(Integer itemId, Integer amount) {
        InventoryItem item = inventoryItemRepository.findInventoryItemById(itemId);
        if (item == null)
            return "Item not found";
        if (amount < 1)
            return "Amount has to be 1 or more";
        if (amount > item.getQuantity())
            return "Amount is more than the available quantity";
        item.setQuantity(item.getQuantity() - amount);
        item.setLastConsumedDate(LocalDate.now());
        inventoryItemRepository.save(item);
        return "success";
    }

    public List<InventoryItem> nearExpiry(Integer labId) {
        if (labRepository.findLabById(labId) == null)
            return null;
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
}
