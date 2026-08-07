package com.cafe.velvetbrew.dto;

import com.cafe.velvetbrew.common.enums.OrderStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateOrderStatusRequest {

    private OrderStatus orderStatus;

}
