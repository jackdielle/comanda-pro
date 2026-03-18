package com.saleorigano.config;

import com.saleorigano.model.*;
import com.saleorigano.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.*;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initializeData(
            CustomerRepository customerRepository,
            ProductRepository productRepository,
            OrderRepository orderRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {
        return args -> {
            // Create default admin user
            if (userRepository.count() == 0) {
                AppUser adminUser = AppUser.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("admin123"))
                    .role(UserRole.ROLE_ADMIN)
                    .enabled(true)
                    .build();
                userRepository.save(adminUser);
            }

            // Load test customers
            if (customerRepository.count() == 0) {
                customerRepository.saveAll(Arrays.asList(
                    Customer.builder()
                        .phoneNumber("3201234567")
                        .name("Giovanni Rossi")
                        .address("Via Roma 10")
                        .intercom("1")
                        .zone("Centro")
                        .build(),
                    Customer.builder()
                        .phoneNumber("3209876543")
                        .name("Maria Bianchi")
                        .address("Corso Garibaldi 22")
                        .intercom("5B")
                        .zone("Est")
                        .build(),
                    Customer.builder()
                        .phoneNumber("3215554444")
                        .name("Antonio Verdi")
                        .address("Piazza del Duomo 5")
                        .intercom("3")
                        .zone("Centro")
                        .build()
                ));
            }

            // Load products
            if (productRepository.count() == 0) {
                List<Product> products = new ArrayList<>();

                // Classic Pizzas
                products.addAll(Arrays.asList(
                    Product.builder().name("Margherita").price(5.3).category(Product.Category.CLASSIC).build(),
                    Product.builder().name("Rossa").price(4.0).category(Product.Category.CLASSIC).build(),
                    Product.builder().name("Marinara").price(4.5).category(Product.Category.CLASSIC).build(),
                    Product.builder().name("Diavola").price(6.5).category(Product.Category.CLASSIC).build(),
                    Product.builder().name("Capricciosa").price(7.5).category(Product.Category.CLASSIC).build(),
                    Product.builder().name("Quattro Formaggi").price(6.5).category(Product.Category.CLASSIC).build()
                ));

                // Special Pizzas
                products.addAll(Arrays.asList(
                    Product.builder().name("BBQ Chicken").price(8.0).category(Product.Category.SPECIAL).build(),
                    Product.builder().name("Parmigiana").price(7.5).category(Product.Category.SPECIAL).build(),
                    Product.builder().name("Salsiccia e Friarielli").price(7.0).category(Product.Category.SPECIAL).build()
                ));

                // Pinse (Gourmet)
                products.addAll(Arrays.asList(
                    Product.builder().name("Pinsa Prosciutto Crudo").price(9.0).category(Product.Category.PINSE).build(),
                    Product.builder().name("Pinsa Burrata").price(9.5).category(Product.Category.PINSE).build(),
                    Product.builder().name("Pinsa Mortadella").price(8.5).category(Product.Category.PINSE).build()
                ));

                // Panozzi
                products.addAll(Arrays.asList(
                    Product.builder().name("Panozzi Ricotta").price(4.5).category(Product.Category.PANOZZI).build(),
                    Product.builder().name("Panozzi Spinaci").price(4.0).category(Product.Category.PANOZZI).build()
                ));

                // Rolled
                products.addAll(Arrays.asList(
                    Product.builder().name("Rolled Salsiccia").price(5.5).category(Product.Category.ROLLED).build(),
                    Product.builder().name("Rolled Formaggio").price(5.0).category(Product.Category.ROLLED).build()
                ));

                // Fried
                products.addAll(Arrays.asList(
                    Product.builder().name("Croquette").price(3.5).category(Product.Category.FRIED).build(),
                    Product.builder().name("Supplì").price(4.0).category(Product.Category.FRIED).build(),
                    Product.builder().name("Arancini").price(3.0).category(Product.Category.FRIED).build()
                ));

                // Beverages
                products.addAll(Arrays.asList(
                    Product.builder().name("Coca Cola").price(2.0).category(Product.Category.BEVERAGES).build(),
                    Product.builder().name("Sprite").price(2.0).category(Product.Category.BEVERAGES).build(),
                    Product.builder().name("Birra").price(3.5).category(Product.Category.BEVERAGES).build(),
                    Product.builder().name("Acqua Frizzante").price(1.5).category(Product.Category.BEVERAGES).build()
                ));

                // Desserts
                products.addAll(Arrays.asList(
                    Product.builder().name("Tiramisu").price(5.0).category(Product.Category.DESSERTS).build(),
                    Product.builder().name("Panna Cotta").price(4.5).category(Product.Category.DESSERTS).build(),
                    Product.builder().name("Cannoli Siciliani").price(3.5).category(Product.Category.DESSERTS).build()
                ));

                productRepository.saveAll(products);
            }
        };
    }

}
