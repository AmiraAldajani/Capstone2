package com.example.labsurplus.Repository;

import com.example.labsurplus.Model.Transfer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransferRepository extends JpaRepository<Transfer, Integer> {
    Transfer findTransferById(Integer id);
    boolean existsByOfferId(Integer offerId);
    boolean existsByToLabId(Integer toLabId);
    List<Transfer> findAllByToLabIdAndReceivedAtIsNotNull(Integer toLabId);
    List<Transfer> findAllByToLabIdAndReceivedAtIsNull(Integer toLabId);       // [جديد]
    List<Transfer> findAllByFromLabIdAndReceivedAtIsNotNull(Integer fromLabId); // [جديد]
}
