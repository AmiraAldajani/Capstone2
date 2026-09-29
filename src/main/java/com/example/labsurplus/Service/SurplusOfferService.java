package com.example.labsurplus.Service;

import com.example.labsurplus.Api.ApiException;
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
import org.springframework.transaction.annotation.Transactional;

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
    private final EmailService emailService;

    public List<SurplusOffer> getAllOffers() {
        return surplusOfferRepository.findAll();
    }

    public void addOffer(SurplusOffer offer) {
        InventoryItem item = inventoryItemRepository.findInventoryItemById(offer.getItemId());
        if (item == null)
            throw new ApiException("Item not found");
        if (!item.getExpiryDate().isAfter(LocalDate.now()))
            throw new ApiException("Can't offer an expired item");
        if (!offer.getAnnouncedUntil().isBefore(item.getExpiryDate()))
            throw new ApiException("Announcement has to end before the item expires");
        if (offer.getQuantity() > item.getQuantity() - reservedQuantity(item.getId()))
            throw new ApiException("Offered quantity is more than what the lab has available");
        offer.setId(null);
        offer.setDonorLabId(item.getLabId());
        offer.setStatus("announced");
        if (offer.getUrgent() == null)
            offer.setUrgent(false);
        surplusOfferRepository.save(offer);
    }

    public void updateOffer(Integer id, SurplusOffer offer) {
        SurplusOffer old = surplusOfferRepository.findSurplusOfferById(id);
        if (old == null)
            throw new ApiException("Offer not found");
        if (!old.getStatus().equals("announced"))
            throw new ApiException("Only announced offers can be updated");
        InventoryItem item = inventoryItemRepository.findInventoryItemById(old.getItemId());
        // [جديد]
        if (!offer.getAnnouncedUntil().isBefore(item.getExpiryDate()))
            throw new ApiException("Announcement has to end before the item expires");
        int reservedByOthers = reservedQuantity(item.getId()) - old.getQuantity();
        if (offer.getQuantity() > item.getQuantity() - reservedByOthers)
            throw new ApiException("Offered quantity is more than what the lab has available");
        old.setQuantity(offer.getQuantity());
        old.setAnnouncedUntil(offer.getAnnouncedUntil());
        old.setUrgent(offer.getUrgent() != null ? offer.getUrgent() : false);
        surplusOfferRepository.save(old);
    }

    public void deleteOffer(Integer id) {
        SurplusOffer offer = surplusOfferRepository.findSurplusOfferById(id);
        if (offer == null)
            throw new ApiException("Offer not found");
        if (surplusRequestRepository.existsByOfferId(id) || transferRepository.existsByOfferId(id))
            throw new ApiException("Can't delete an offer that has requests or a transfer");
        surplusOfferRepository.delete(offer);
    }
    public int reservedQuantity(Integer itemId) {
        int total = 0;
        List<String> active = List.of("announced", "requested", "approved");
        for (SurplusOffer o : surplusOfferRepository.findAllByItemIdAndStatusIn(itemId, active))
            total = total + o.getQuantity();
        return total;
    }


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

    public List<SurplusOffer> availableForLab(Integer labId) {
        if (labRepository.findLabById(labId) == null)
            throw new ApiException("Lab not found");
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

    public List<SurplusOffer> urgentOffers() {
        List<SurplusOffer> result = new ArrayList<>();
        for (SurplusOffer o : openOffers())
            if (Boolean.TRUE.equals(o.getUrgent()))
                result.add(o);
        return result;
    }

    // يقفل العروض اللي انتهت مدتها وما أحد طلبها ويرجع عددها
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

    // المتبرع يسحب عرضه قبل ما يوافق على أي طلب والطلبات المعلقة عليه تنرفض
    @Transactional // [جديد]
    public void closeOffer(Integer offerId) {
        SurplusOffer offer = surplusOfferRepository.findSurplusOfferById(offerId);
        if (offer == null)
            throw new ApiException("Offer not found");
        if (!offer.getStatus().equals("announced") && !offer.getStatus().equals("requested"))
            throw new ApiException("Only announced or requested offers can be closed");
        List<SurplusRequest> rejectedNow = new ArrayList<>();
        for (SurplusRequest r : surplusRequestRepository.findAllByOfferId(offerId))
            if (r.getStatus().equals("pending")) {
                r.setStatus("rejected");
                surplusRequestRepository.save(r);
                rejectedNow.add(r);
            }
        offer.setStatus("closed");
        surplusOfferRepository.save(offer);

        // نبلغ المختبرات اللي انقفلت طلباتها
        for (SurplusRequest r : rejectedNow)
            emailService.notifyLab(labRepository.findLabById(r.getRequestingLabId()),
                    "Surplus offer #" + offerId + " was withdrawn",
                    "The donor lab withdrew offer #" + offerId + ", so your request #" + r.getId() + " was closed.");
    }
}