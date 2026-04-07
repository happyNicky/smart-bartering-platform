package com.finalyear.liwatch.barter;


import com.finalyear.liwatch.Post.Post;
import com.finalyear.liwatch.barter.barterenum.BarterType;

import com.finalyear.liwatch.digitalagreement.DigitalAgreement;
import com.finalyear.liwatch.directswap.DirectSwapRequest;
import com.finalyear.liwatch.negotiation.Negotiation;
import com.finalyear.liwatch.userManagement.model.User;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "barters")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Barter {

        @Id @GeneratedValue
        private Long id;

        private LocalDateTime createdAt;

        // owning side
        @OneToOne
        @JoinColumn(name = "request_id")
        private DirectSwapRequest swapRequest;

        // two users only
        @ManyToOne
        private User userA;

        @ManyToOne
        private User userB;

        // what they swap
        @ManyToOne
        private Post postA;

        @ManyToOne(fetch = FetchType.LAZY)
        private Post postB;

        @OneToOne(mappedBy = "barter", cascade = CascadeType.ALL,fetch = FetchType.LAZY)
        private Negotiation negotiation;

        @OneToMany(mappedBy = "barter", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
        private List<DigitalAgreement> agreements;

}
