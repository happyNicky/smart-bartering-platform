package com.finalyear.liwatch.barter.barter_managment;

import com.finalyear.liwatch.barter.Barter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BarterRepository extends JpaRepository<Barter,Long> {
}
