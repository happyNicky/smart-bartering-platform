package com.finalyear.liwatch.barter.barter_managment;

import com.finalyear.liwatch.barter.Barter;
import com.finalyear.liwatch.userManagement.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BarterRepository extends JpaRepository<Barter,Long> {

    List<Barter> getBartersByUserAOrUserB(User userA, User userB);
}
