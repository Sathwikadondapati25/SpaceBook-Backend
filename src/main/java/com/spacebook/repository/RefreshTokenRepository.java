package com.spacebook.repository;

import com.spacebook.entity.RefreshToken;
import com.spacebook.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    List<RefreshToken> findByUser(User user);

    void deleteByToken(String token);

    void deleteByUser(User user);
}
