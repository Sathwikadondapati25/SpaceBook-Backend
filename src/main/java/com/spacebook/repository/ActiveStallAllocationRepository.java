package com.spacebook.repository;

import com.spacebook.entity.ActiveStallAllocation;
import com.spacebook.entity.StallSpace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ActiveStallAllocationRepository extends JpaRepository<ActiveStallAllocation, Long> {

    Optional<ActiveStallAllocation> findByStall(StallSpace stall);

    void deleteByStall(StallSpace stall);
}
