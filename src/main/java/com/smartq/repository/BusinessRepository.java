package com.smartq.repository;

import com.smartq.entity.Business;
import com.smartq.enums.BusinessType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BusinessRepository
        extends JpaRepository<Business, Long> {

    List<Business> findByOwnerId(Long ownerId);
    List<Business> findByBusinessType(BusinessType businessType);
    List<Business> findByCity(String city);
    Optional<Business> findByIdAndOwnerId(Long id, Long ownerId);
    boolean existsByNameAndOwnerId(String name, Long ownerId);
    long countByBusinessType(BusinessType businessType);
}