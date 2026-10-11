package com.spacebook.repository;

import com.spacebook.entity.Event;
import com.spacebook.entity.StallSpace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

@Repository
public interface StallSpaceRepository extends JpaRepository<StallSpace, Long> {

    List<StallSpace> findByEvent(Event event);

    Optional<StallSpace> findByEventAndStallNumber(Event event, String stallNumber);

    List<StallSpace> findByEventAndGridRowAndGridCol(Event event, Integer gridRow, Integer gridCol);

    // Pessimistic lock for approval transaction
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM StallSpace s WHERE s.id = :id")
    Optional<StallSpace> findByIdWithLock(Long id);
}
