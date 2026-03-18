package com.saleorigano.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "products", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"category", "name"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Double price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;

    private Boolean available = true;

    @Column(name = "created_at")
    private Long createdAt = System.currentTimeMillis();

    public enum Category {
        CLASSIC("Classic"),
        SPECIAL("Special"),
        PINSE("Pinse"),
        PANOZZI("Panozzi"),
        ROLLED("Rolled"),
        FRIED("Fried"),
        BEVERAGES("Beverages"),
        DESSERTS("Desserts");

        private final String label;

        Category(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

}
