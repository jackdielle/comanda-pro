package com.comandapro.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyOrderSummaryDTO {
    private LocalDate date;
    private Long totalOrders;
    private Double totalRevenue;
    private Integer totalClassic;
    private Integer totalPanozzi;
    private Integer totalRolled;
    private Integer totalPinse;
}
