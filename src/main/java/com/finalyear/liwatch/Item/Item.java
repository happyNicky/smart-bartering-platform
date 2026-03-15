package com.finalyear.liwatch.Item;


import com.finalyear.liwatch.Item.itemenum.Condition;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;


import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id")
    private Long itemId;

    @Column(name = "post_id", nullable = false)
    private Long postId;

    @Enumerated(EnumType.STRING)
    private Condition condition;

    @Column(name = "estimated_value", precision = 10, scale = 2)
    private BigDecimal estimatedValue;

    @Column(name = "partial_cash_allowed")
    private Boolean partialCashAllowed = false;

    public enum Condition {
        NEW,
        USED
    }
}
