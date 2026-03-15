package com.finalyear.liwatch.barter;


import com.finalyear.liwatch.barter.barterenum.BarterType;
import com.finalyear.liwatch.barter.barterenum.Status;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "barters")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Barter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "barter_id")
    private Long barterId;

    @Enumerated(EnumType.STRING)
    private Status status;

    @Enumerated(EnumType.STRING)
    @Column(name = "barter_type")
    private BarterType barterType;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;




}
