package com.finalyear.liwatch.Item;

import com.finalyear.liwatch.Item.itemenum.Condition;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Data
@Getter
@Setter
public class ItemRequestDto {
    private Condition condition;
    private BigDecimal estimatedValue;
    private Boolean partialCashAllowed = false;
}
