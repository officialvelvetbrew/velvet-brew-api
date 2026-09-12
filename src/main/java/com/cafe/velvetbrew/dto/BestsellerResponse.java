package com.cafe.velvetbrew.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class BestsellerResponse {

    private Long menuItemId;

    private String name;

    private String categoryName;

    private long quantitySold;

    private BigDecimal revenue;
}
