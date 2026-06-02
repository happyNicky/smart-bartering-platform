package com.finalyear.liwatch.digitalagreement.agreement_managment;

import com.finalyear.liwatch.digitalagreement.DigitalAgreement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DigitalAgreementRepository extends JpaRepository<DigitalAgreement,Long> {
    Optional<DigitalAgreement> findByBarterId(Long barterId);
    Optional<DigitalAgreement> findByDocumentHash(String documentHash);
}
