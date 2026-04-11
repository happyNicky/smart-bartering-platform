package com.finalyear.liwatch.userManagement.repository;

import com.finalyear.liwatch.userManagement.model.PasswordResetToken;
import com.finalyear.liwatch.userManagement.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken,Long> {
    Optional<PasswordResetToken> findByToken(String token);
    void deleteByUser(User user);
}
