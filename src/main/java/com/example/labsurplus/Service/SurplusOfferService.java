package com.example.labsurplus.Service;

import com.example.labsurplus.Model.InventoryItem;
import com.example.labsurplus.Model.SurplusOffer;
import com.example.labsurplus.Model.SurplusRequest;
import com.example.labsurplus.Repository.InventoryItemRepository;
import com.example.labsurplus.Repository.LabRepository;
import com.example.labsurplus.Repository.SurplusOfferRepository;
import com.example.labsurplus.Repository.SurplusRequestRepository;
import com.example.labsurplus.Repository.TransferRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SurplusOfferService {

    private final SurplusOfferRepository surplusOfferRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final SurplusRequestRepository surplusRequestRepository;
    private final TransferRepository transferRepository;
    private final LabRepository labRepository;

    public List<SurplusOffer> getAllOffers() {
        return surplusOfferRepository.findAll();
    }

    public String addOffer(SurplusOffer offer) {
        InventoryItem item = inventoryItemRepository.findInventoryItemById(offer.getItemId());
        if (item == null)
            return "Item not found";
        if (offer.getQuantity() > item.getQuantity())
            return "Offered quantity is more than what the lab has";
        offer.setDonorLabId(item.getLabId()); // المتبرع = مالك الصنف
        offer.setStatus("announced");         // الحالة يحددها النظام
        if (offer.getUrgent() == null)
            offer.setUrgent(false);
        surplusOfferRepository.save(offer);
        return "success";
    }

    public String updateOffer(Integer id, SurplusOffer offer) {
        SurplusOffer old = surplusOfferRepository.findSurplusOfferById(id);
        if (old == null)
            return "Offer not found";
        if (!old.getStatus().equals("announced"))
            return "Only announced offers can be updated";
        InventoryItem item = inventoryItemRepository.findInventoryItemById(old.getItemId());
        if (offer.getQuantity() > item.getQuantity())
            return "Offered quantity is more than what the lab has";
        old.setQuantity(offer.getQuantity());
        old.setAnnouncedUntil(offer.getAnnouncedUntil());
        old.setUrgent(offer.getUrgent() != null ? offer.getUrgent() : false);
        surplusOfferRepository.save(old);
        return "success";
    }

    public String deleteOffer(Integer id) {
        SurplusOffer offer = surplusOfferRepository.findSurplusOfferById(id);
        if (offer == null)
            return "Offer not found";
        if (surplusRequestRepository.existsByOfferId(id) || transferRepository.existsByOfferId(id))
            return "Can't delete an offer that has requests or a transfer";
        surplusOfferRepository.delete(offer);
        return "success";
    }

    // ---------------- Extra endpoints ----------------

    // العروض المفتوحة (announced أو requested) اللي ما انتهت مدتها
    // صارت public عشان نعرضها كـ endpoint، وتستخدمها availableForLab و byCategory
    public List<SurplusOffer> openOffers() {
        LocalDate today = LocalDate.now();
        List<SurplusOffer> open = new ArrayList<>();
        List<SurplusOffer> all = new ArrayList<>();
        all.addAll(surplusOfferRepository.findAllByStatus("announced"));
        all.addAll(surplusOfferRepository.findAllByStatus("requested"));
        for (SurplusOffer o : all)
            if (!o.getAnnouncedUntil().isBefore(today))
                open.add(o);
        return open;
    }

    // العروض اللي يقدر مختبر معين يطلبها (مو من نفس المختبر)
    // ترجع null لو المختبر مو موجود
    public List<SurplusOffer> availableForLab(Integer labId) {
        if (labRepository.findLabById(labId) == null)
            return null;
        List<SurplusOffer> result = new ArrayList<>();
        for (SurplusOffer o : openOffers())
            if (!o.getDonorLabId().equals(labId))
                result.add(o);
        return result;
    }

    // العروض المفتوحة حسب فئة الصنف
    public List<SurplusOffer> byCategory(String category) {
        List<SurplusOffer> result = new ArrayList<>();
        for (SurplusOffer o : openOffers()) {
            InventoryItem item = inventoryItemRepository.findInventoryItemById(o.getItemId());
            if (item.getCategory().equalsIgnoreCase(category))
                result.add(o);
        }
        return result;
    }

    // يقفل العروض اللي انتهت مدتها وما أحد طلبها، ويرجع عددها
    public int expireOld() {
        LocalDate today = LocalDate.now();
        int count = 0;
        for (SurplusOffer o : surplusOfferRepository.findAllByStatus("announced"))
            if (o.getAnnouncedUntil().isBefore(today)) {
                o.setStatus("expired");
                surplusOfferRepository.save(o);
                count++;
            }
        return count;
    }

    // المتبرع يسحب عرضه قبل ما يوافق على أي طلب، والطلبات المعلقة عليه تنرفض تلقائيًا
    public String closeOffer(Integer offerId) {
        SurplusOffer offer = surplusOfferRepository.findSurplusOfferById(offerId);
        if (offer == null)
            return "Offer not found";
        if (!offer.getStatus().equals("announced") && !offer.getStatus().equals("requested"))
            return "Only announced or requested offers can be closed";
        for (SurplusRequest r : surplusRequestRepository.findAllByOfferId(offerId))
            if (r.getStatus().equals("pending")) {
                r.setStatus("rejected");
                surplusRequestRepository.save(r);
            }
        offer.setStatus("closed");
        surplusOfferRepository.save(offer);
        return "success";
    }
}
