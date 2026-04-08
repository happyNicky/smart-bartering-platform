package com.finalyear.liwatch.digitalagreement.agreement_managment;

import com.finalyear.liwatch.digitalagreement.DigitalAgreement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DigitalAgreementRepository extends JpaRepository<DigitalAgreement,Long> {
}
