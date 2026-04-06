package com.finalyear.liwatch.userprofile;

import com.finalyear.liwatch.userManagement.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProfileRepository extends JpaRepository<UserProfile,Long> {
    Optional<UserProfile> findByUser(User user);
}
