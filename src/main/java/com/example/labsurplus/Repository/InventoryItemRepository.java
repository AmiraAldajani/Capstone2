package com.example.labsurplus.Repository;

import com.example.labsurplus.Model.InventoryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InventoryItemRepository extends JpaRepository<InventoryItem, Integer> {
    InventoryItem findInventoryItemById(Integer id);
    boolean existsByLabId(Integer labId);
    List<InventoryItem> findAllByLabId(Integer labId);
}
