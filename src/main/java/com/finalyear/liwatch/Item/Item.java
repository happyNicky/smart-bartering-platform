package com.finalyear.liwatch.Item;


import com.finalyear.liwatch.Item.itemenum.Condition;
import com.finalyear.liwatch.Post.Post;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;


import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Entity
@Table(name = "items")
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@SuperBuilder
@DiscriminatorValue("item")
public class Item extends Post {

    @Enumerated(EnumType.STRING)
    @Column(name="item_condition")
    private Condition condition;

    @Column(name = "estimated_value", precision = 10, scale = 2)
    private BigDecimal estimatedValue;

    @Column(name = "partial_cash_allowed")
    private Boolean partialCashAllowed = false;


}
