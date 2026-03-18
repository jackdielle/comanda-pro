package com.saleorigano.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderLineDTO {
    private Long id;
    private Long productId;
    private String productName;
    private String productCategory;
    @NotNull(message = "Quantity is required")
    @Min(1)
    private Integer quantity;
    @NotNull(message = "Price is required")
    private Double unitPrice;
    private Double subtotal;
    private String notes;
    private Boolean isPinsa = false;
    private Boolean noLactose = false;
}
