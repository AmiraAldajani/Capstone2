package com.example.labsurplus.Repository;

import com.example.labsurplus.Model.SurplusOffer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SurplusOfferRepository extends JpaRepository<SurplusOffer, Integer> {
    SurplusOffer findSurplusOfferById(Integer id);
    boolean existsByItemId(Integer itemId);
    List<SurplusOffer> findAllByStatus(String status);
    List<SurplusOffer> findAllByItemIdAndStatusIn(Integer itemId, List<String> statuses); // [جديد]
}
