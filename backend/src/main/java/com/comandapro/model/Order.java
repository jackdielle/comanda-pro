package com.comandapro.model;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JsonManagedReference
    @Builder.Default
    private List<OrderLine> lines = new ArrayList<>();

    @Column(nullable = false)
    private Double total = 0.0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private OrderStatus status = OrderStatus.IN_PREPARATION;

    @Column(name = "delivery_time")
    private String deliveryTime;

    @Column(name = "notes")
    private String notes;

    @Column(name = "created_at")
    private Long createdAt = System.currentTimeMillis();

    @Column(name = "updated_at")
    private Long updatedAt = System.currentTimeMillis();

    // Counters for summary
    @Column(name = "count_classic")
    @Builder.Default
    private Integer countClassic = 0;

    @Column(name = "count_panozzi")
    @Builder.Default
    private Integer countPanozzi = 0;

    @Column(name = "count_rolled")
    @Builder.Default
    private Integer countRolled = 0;

    @Column(name = "count_pinse")
    @Builder.Default
    private Integer countPinse = 0;

    @Column(name = "payment_method")
    @Builder.Default
    private String paymentMethod = "CASH"; // CASH or CREDIT_CARD

    @Column(name = "customer_data_removed")
    @Builder.Default
    private Boolean customerDataRemoved = false;

    public enum OrderStatus {
        IN_PREPARATION("In Preparation"),
        READY("Ready"),
        IN_DELIVERY("In Delivery"),
        DELIVERED("Delivered"),
        CANCELLED("Cancelled");

        private final String label;

        OrderStatus(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    public void calculateTotal() {
        this.total = this.lines.stream()
            .mapToDouble(OrderLine::getSubtotal)
            .sum();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = System.currentTimeMillis();
    }

}
