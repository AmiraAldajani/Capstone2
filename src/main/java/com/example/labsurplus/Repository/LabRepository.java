package com.example.labsurplus.Repository;

import com.example.labsurplus.Model.Lab;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LabRepository extends JpaRepository<Lab, Integer> {
    Lab findLabById(Integer id);
    boolean existsByName(String name);
}
