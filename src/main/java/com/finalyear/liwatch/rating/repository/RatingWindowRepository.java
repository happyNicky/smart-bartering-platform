package com.finalyear.liwatch.rating.repository;

import com.finalyear.liwatch.rating.RatingWindow;
import com.finalyear.liwatch.rating.enums.RatingWindowStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RatingWindowRepository extends JpaRepository<RatingWindow, Long> {

    Optional<RatingWindow> findByBarterId(Long barterId);

    List<RatingWindow> findByStatusAndDeadlineBefore(RatingWindowStatus status, LocalDateTime deadline);
}
