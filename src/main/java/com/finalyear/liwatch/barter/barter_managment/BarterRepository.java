package com.finalyear.liwatch.barter.barter_managment;

import com.finalyear.liwatch.barter.Barter;
import com.finalyear.liwatch.userManagement.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BarterRepository extends JpaRepository<Barter,Long> {

    List<Barter> getBartersByUserAOrUserB(User userA, User userB);

    @Query("SELECT COUNT(b) FROM Barter b WHERE b.userA.id = :userId OR b.userB.id = :userId")
    long countBartersForUser(@Param("userId") Long userId);

    @Query("""
            SELECT COUNT(DISTINCT b.id) FROM Barter b
            JOIN b.agreements a
            WHERE (b.userA.id = :userId OR b.userB.id = :userId)
            AND a.type = com.finalyear.liwatch.digitalagreement.enum_agreement.AgreementType.FINALIZED
            """)
    long countCompletedBartersForUser(@Param("userId") Long userId);
}
