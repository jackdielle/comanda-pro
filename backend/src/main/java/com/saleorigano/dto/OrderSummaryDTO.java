package com.saleorigano.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderSummaryDTO {
    private Long totalOrders;
    private Double totalRevenue;
    private Integer totalClassic;
    private Integer totalPanozzi;
    private Integer totalRolled;
    private Integer totalPinse;
    private java.util.Map<String, Long> ordersByStatus;
}
