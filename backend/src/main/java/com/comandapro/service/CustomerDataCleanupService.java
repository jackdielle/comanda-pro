package com.comandapro.service;

import com.comandapro.model.Order;
import com.comandapro.model.Customer;
import com.comandapro.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CustomerDataCleanupService {

    private final OrderRepository orderRepository;

    /**
     * Removes customer data from orders older than today at 00:00
     * Runs daily at 01:00 AM (3600000 ms = 1 hour after midnight)
     */
    @Scheduled(cron = "0 1 * * *") // 01:00 AM every day
    public void removeOldCustomerData() {
        log.info("Starting customer data cleanup...");

        try {
            LocalDate today = LocalDate.now();
            long startOfDay = today.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();

            // Find all orders from before today that still have customer data
            List<Order> oldOrders = orderRepository.findAll().stream()
                    .filter(order -> order.getCreatedAt() < startOfDay && !order.getCustomerDataRemoved())
                    .toList();

            log.info("Found {} orders to clean", oldOrders.size());

            for (Order order : oldOrders) {
                // Keep the customer relationship for referential integrity, but clear sensitive data
                Customer customer = order.getCustomer();
                if (customer != null) {
                    // Mark the order as data removed instead of deleting
                    order.setCustomerDataRemoved(true);
                    orderRepository.save(order);
                    log.debug("Removed customer data from order {}", order.getId());
                }
            }

            log.info("Customer data cleanup completed successfully. Cleaned {} orders", oldOrders.size());
        } catch (Exception e) {
            log.error("Error during customer data cleanup", e);
        }
    }
}
