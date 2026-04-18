package com.finalyear.liwatch.negotiation.negotiation_management;

import com.finalyear.liwatch.barter.Barter;
import com.finalyear.liwatch.negotiation.Negotiation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NegotiationRepository extends JpaRepository<Negotiation,Long> {
    List<Negotiation> findByBarter(Barter barter);

}
