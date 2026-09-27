package com.example.labsurplus.Service;

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
    private final LabRepository labRepository;   // [جديد]
    private final EmailService emailService;     // [جديد]

    public List<Transfer> getAllTransfers() {
        return transferRepository.findAll();
    }

    public String addTransfer(Transfer transfer) {
        SurplusOffer offer = surplusOfferRepository.findSurplusOfferById(transfer.getOfferId());
        if (offer == null)
            return "Offer not found";
        if (!offer.getStatus().equals("approved"))
            return "The offer has to be approved before creating a transfer";
        if (transferRepository.existsByOfferId(transfer.getOfferId()))
            return "This offer already has a transfer";
        SurplusRequest approved = surplusRequestRepository.findSurplusRequestByOfferIdAndStatus(offer.getId(), "approved");
        if (!transfer.getToLabId().equals(approved.getRequestingLabId()))
            return "Receiving lab has to be the lab whose request was approved";
        transfer.setId(null);                         // [جديد] لو انرسل id ما يكتب فوق تحويل موجود
        transfer.setFromLabId(offer.getDonorLabId()); // المرسل = المتبرع
        if (transfer.getType() == null)
            transfer.setType("internal");
        // بيانات الاستلام تتعبى من endpoint الاستلام بس
        transfer.setReceivedBy(null);
        transfer.setReceivedTemperature(null);
        transfer.setReceivedAt(null);
        transferRepository.save(transfer);

        // [جديد] نبلغ المختبر المستلم إن الشحنة في الطريق
        Lab donor = labRepository.findLabById(offer.getDonorLabId());
        emailService.notifyLab(labRepository.findLabById(transfer.getToLabId()),
                "Surplus transfer #" + transfer.getId() + " is on its way",
                "Transfer #" + transfer.getId() + " for offer #" + offer.getId() + " is coming from "
                        + (donor != null ? donor.getName() : "the donor lab")
                        + ". Please record the temperature when you receive it.");
        return "success";
    }

    // التحديث للنوع بس. المختبر المستلم يحدده الطلب المعتمد، وبيانات الاستلام من /receive
    public String updateTransfer(Integer id, Transfer transfer) {
        Transfer old = transferRepository.findTransferById(id);
        if (old == null)
            return "Transfer not found";
        if (old.getReceivedAt() != null)
            return "Can't update a transfer that was already received";
        if (transfer.getType() != null)
            old.setType(transfer.getType());
        transferRepository.save(old);
        return "success";
    }

    public String deleteTransfer(Integer id) {
        Transfer transfer = transferRepository.findTransferById(id);
        if (transfer == null)
            return "Transfer not found";
        if (transfer.getReceivedAt() != null)
            return "Can't delete a transfer that was already received";
        transferRepository.delete(transfer);
        return "success";
    }

    // ---------------- Extra endpoints ----------------

    // تأكيد الاستلام: ينقل الكمية من مخزون المتبرع لمخزون المستفيد
    @Transactional // [جديد] ثلاث عمليات حفظ مرتبطة ببعض، لو وحدة فشلت يرجع كل شيء
    public String receive(Integer transferId, String receivedBy, Double temperature) {
        Transfer transfer = transferRepository.findTransferById(transferId);
        if (transfer == null)
            return "Transfer not found";
        if (transfer.getReceivedAt() != null)
            return "This transfer was already received";

        SurplusOffer offer = surplusOfferRepository.findSurplusOfferById(transfer.getOfferId());
        SurplusRequest approved = surplusRequestRepository.findSurplusRequestByOfferIdAndStatus(offer.getId(), "approved");
        InventoryItem donorItem = inventoryItemRepository.findInventoryItemById(offer.getItemId());
        int quantity = approved.getQuantity();

        if (donorItem.getQuantity() < quantity)
            return "Donor lab doesn't have enough quantity anymore";

        // 1) ننقص من المتبرع
        donorItem.setQuantity(donorItem.getQuantity() - quantity);
        inventoryItemRepository.save(donorItem);

        // 2) نضيف صنف جديد لمخزون المستفيد بنفس البيانات (نفس رقم التشغيلة والصلاحية)
        InventoryItem receivedItem = new InventoryItem(null, transfer.getToLabId(), donorItem.getName(),
                donorItem.getCategory(), donorItem.getLotNumber(), quantity, donorItem.getUnit(),
                donorItem.getUnitPrice(), donorItem.getExpiryDate(), donorItem.getStorageCondition(), null);
        inventoryItemRepository.save(receivedItem);

        // 3) نسجل بيانات الاستلام ونحدّث حالة العرض
        transfer.setReceivedBy(receivedBy);
        transfer.setReceivedTemperature(temperature);
        transfer.setReceivedAt(LocalDateTime.now());
        transferRepository.save(transfer);

        offer.setStatus("transferred");
        surplusOfferRepository.save(offer);

        // [جديد] نبلغ المتبرع إن الشحنة وصلت
        emailService.notifyLab(labRepository.findLabById(transfer.getFromLabId()),
                "Surplus transfer #" + transferId + " was received",
                "Transfer #" + transferId + " (" + quantity + " " + donorItem.getUnit() + " of " + donorItem.getName()
                        + ") was received by " + receivedBy + " at " + temperature + " C.");
        return "success";
    }

    // [جديد] التحويلات اللي في الطريق لمختبر معين وما انستلمت
    // ترجع null لو المختبر مو موجود
    public List<Transfer> pendingForLab(Integer labId) {
        if (labRepository.findLabById(labId) == null)
            return null;
        return transferRepository.findAllByToLabIdAndReceivedAtIsNull(labId);
    }
}
