package com.example.labsurplus.Repository;

import com.example.labsurplus.Model.SurplusRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SurplusRequestRepository extends JpaRepository<SurplusRequest, Integer> {
    SurplusRequest findSurplusRequestById(Integer id);
    boolean existsByOfferId(Integer offerId);
    boolean existsByOfferIdAndRequestingLabId(Integer offerId, Integer requestingLabId);
    boolean existsByRequestingLabId(Integer requestingLabId);
    boolean existsByOfferIdAndStatus(Integer offerId, String status);
    List<SurplusRequest> findAllByOfferId(Integer offerId);
    List<SurplusRequest> findAllByOfferIdAndStatus(Integer offerId, String status);
    SurplusRequest findSurplusRequestByOfferIdAndStatus(Integer offerId, String status);
    List<SurplusRequest> findAllByRequestingLabId(Integer requestingLabId); // [جديد]
}
