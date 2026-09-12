package com.cafe.velvetbrew.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StatusCountResponse {

    private String status;

    private long count;
}
