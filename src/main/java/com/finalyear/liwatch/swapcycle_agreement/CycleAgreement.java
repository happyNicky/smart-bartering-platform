package com.finalyear.liwatch.swapcycle_agreement;


import com.finalyear.liwatch.digitalagreement.DigitalAgreement;
import com.finalyear.liwatch.swapcycle.SwapCycle;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "swap_cycle_agreements")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class CycleAgreement {

    @Id @GeneratedValue
    private Long id;

    @ManyToOne
    private SwapCycle swapCycle;

    @OneToOne
    private DigitalAgreement agreement;
}