package com.comandapro.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDTO {
    private Long id;
    private CustomerDTO customer;
    @NotEmpty(message = "Order must contain at least one product")
    private java.util.List<OrderLineDTO> lines;
    private Double total;
    private String status;
    private String deliveryTime;
    private String notes;
    private Integer countClassic;
    private Integer countPanozzi;
    private Integer countRolled;
    private Integer countPinse;
    private Long createdAt;
    private String paymentMethod;
}
