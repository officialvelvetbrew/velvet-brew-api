package com.cafe.velvetbrew.dto;


import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class OrderItemResponse {

    private Long menuId;

    private String menuName;

    private Integer quantity;

    private BigDecimal unitPrice;

    private BigDecimal totalPrice;

}