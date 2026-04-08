package com.finalyear.liwatch.directswap.directswap_managment;

import com.finalyear.liwatch.directswap.DirectSwapRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DirectSwapRequestRepository  extends JpaRepository<DirectSwapRequest,Long> {
}
