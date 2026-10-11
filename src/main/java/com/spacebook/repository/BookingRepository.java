package com.spacebook.repository;

import com.spacebook.entity.Booking;
import com.spacebook.entity.Booking.BookingStatus;
import com.spacebook.entity.StallSpace;
import com.spacebook.entity.User;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByVendor(User vendor);

    List<Booking> findByStall(StallSpace stall);

    List<Booking> findByStatus(BookingStatus status);

    List<Booking> findByStallAndStatus(StallSpace stall, BookingStatus status);

    // Check for active request (not cancelled or rejected) by vendor for stall
    @Query("SELECT b FROM Booking b WHERE b.stall.id = :stallId AND b.vendor.id = :vendorId " +
           "AND b.status NOT IN ('CANCELLED', 'REJECTED')")
    List<Booking> findActiveRequestByVendorAndStall(@Param("stallId") Long stallId,
                                                   @Param("vendorId") Long vendorId);

    // Check for active request with pessimistic write lock inside transactions
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM Booking b WHERE b.stall.id = :stallId AND b.vendor.id = :vendorId " +
           "AND b.status NOT IN ('CANCELLED', 'REJECTED')")
    List<Booking> findActiveRequestByVendorAndStallWithLock(@Param("stallId") Long stallId,
                                                           @Param("vendorId") Long vendorId);

    // Find all pending requests for a stall (for rejection during approval)
    @Query("SELECT b FROM Booking b WHERE b.stall.id = :stallId AND b.status = 'REQUESTED'")
    List<Booking> findPendingRequestsByStall(@Param("stallId") Long stallId);
}
