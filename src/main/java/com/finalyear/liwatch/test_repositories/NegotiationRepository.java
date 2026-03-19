package com.finalyear.liwatch.test_repositories;

import com.finalyear.liwatch.negotiation.Negotiation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NegotiationRepository extends JpaRepository<Negotiation,Long> {
}
