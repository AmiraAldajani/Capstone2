package com.example.labsurplus.Service;

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

    public String addItem(InventoryItem item) {
        if (labRepository.findLabById(item.getLabId()) == null)
            return "Lab not found";
        item.setId(null); // [جديد] لو انرسل id بالـ body ما يكتب فوق صنف موجود
        inventoryItemRepository.save(item);
        return "success";
    }

    public String updateItem(Integer id, InventoryItem item) {
        InventoryItem old = inventoryItemRepository.findInventoryItemById(id);
        if (old == null)
            return "Item not found";
        if (labRepository.findLabById(item.getLabId()) == null)
            return "Lab not found";
        // [جديد] صنف عليه عروض ما ينقل لمختبر ثاني، وإلا يصير donorLabId في العرض غلط
        if (!old.getLabId().equals(item.getLabId()) && surplusOfferRepository.existsByItemId(id))
            return "Can't change the lab of an item that has surplus offers";
        // [جديد] الكمية ما تنزل تحت المحجوز في عروض مفتوحة
        if (item.getQuantity() < surplusOfferService.reservedQuantity(id))
            return "Quantity can't be less than what is reserved in open offers";
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

    // تسجيل صرف من الصنف: ينقص الكمية ويحدّث تاريخ آخر صرف
    public String consume(Integer itemId, Integer amount) {
        InventoryItem item = inventoryItemRepository.findInventoryItemById(itemId);
        if (item == null)
            return "Item not found";
        if (amount < 1)
            return "Amount has to be 1 or more";
        // [تعديل] ما ينصرف من الكمية المحجوزة في عروض فائض
        if (amount > item.getQuantity() - surplusOfferService.reservedQuantity(itemId))
            return "Amount is more than the available quantity (part of it is reserved in surplus offers)";
        item.setQuantity(item.getQuantity() - amount);
        item.setLastConsumedDate(LocalDate.now());
        inventoryItemRepository.save(item);
        return "success";
    }

    // التنبيه المبكر: أصناف تنتهي خلال 6 أشهر وما انصرف منها شيء من 3 أشهر
    // ترجع null لو المختبر مو موجود
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

    // [جديد] يرسل التنبيه المبكر لإيميل المختبر، عشان يعرض الأصناف كفائض قبل ما تضيع
    public String notifyNearExpiry(Integer labId) {
        Lab lab = labRepository.findLabById(labId);
        if (lab == null)
            return "Lab not found";
        if (lab.getEmail() == null || lab.getEmail().isBlank())
            return "This lab has no email";
        List<InventoryItem> items = nearExpiry(labId);
        if (items.isEmpty())
            return "No items close to expiry, nothing to send";
        StringBuilder text = new StringBuilder();
        text.append("These items expire within 6 months and haven't been used in the last 3 months.\n");
        text.append("Consider offering them as surplus so another lab can use them:\n\n");
        for (InventoryItem item : items)
            text.append("- ").append(item.getName())
                    .append(" | lot ").append(item.getLotNumber())
                    .append(" | ").append(item.getQuantity()).append(" ").append(item.getUnit())
                    .append(" | expires ").append(item.getExpiryDate()).append("\n");
        emailService.notifyLab(lab, "Items close to expiry in " + lab.getName(), text.toString());
        return "success";
    }
}
