package com.comandapro.model;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
@Table(name = "order_lines")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "order_id", nullable = false)
    @JsonBackReference
    private Order order;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private Double unitPrice;

    @Column(name = "line_notes")
    private String notes;

    // Special flags
    @Column(name = "is_pinsa")
    private Boolean isPinsa = false;

    @Column(name = "no_lactose")
    private Boolean noLactose = false;

    @Column(name = "created_at")
    private Long createdAt = System.currentTimeMillis();

    public Double getSubtotal() {
        return unitPrice * quantity;
    }

}
