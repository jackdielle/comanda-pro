package com.comandapro.service;

import com.comandapro.dto.CustomerDTO;
import com.comandapro.dto.DailyOrderSummaryDTO;
import com.comandapro.dto.OrderDTO;
import com.comandapro.dto.OrderSummaryDTO;
import com.comandapro.dto.OrderLineDTO;
import com.comandapro.model.Customer;
import com.comandapro.model.Order;
import com.comandapro.model.Product;
import com.comandapro.model.OrderLine;
import com.comandapro.repository.CustomerRepository;
import com.comandapro.repository.OrderRepository;
import com.comandapro.repository.ProductRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.print.PrintService;
import javax.print.PrintServiceLookup;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;

    public OrderDTO createOrder(OrderDTO dto) {
        Customer customer = customerRepository.findById(dto.getCustomer().getId())
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        Order order = Order.builder()
                .customer(customer)
                .deliveryTime(dto.getDeliveryTime())
                .notes(dto.getNotes())
                .paymentMethod(dto.getPaymentMethod() != null ? dto.getPaymentMethod() : "CASH")
                .build();

        for (OrderLineDTO lineDTO : dto.getLines()) {
            Product product = productRepository.findById(lineDTO.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found"));

            OrderLine line = OrderLine.builder()
                    .order(order)
                    .product(product)
                    .quantity(lineDTO.getQuantity())
                    .unitPrice(product.getPrice())
                    .notes(lineDTO.getNotes())
                    .isPinsa(lineDTO.getIsPinsa() != null ? lineDTO.getIsPinsa() : false)
                    .noLactose(lineDTO.getNoLactose() != null ? lineDTO.getNoLactose() : false)
                    .build();

            order.getLines().add(line);
            updateCounters(order, product.getCategory(), lineDTO.getQuantity(), lineDTO.getIsPinsa());
        }

        order.calculateTotal();
        Order saved = orderRepository.save(order);

        // Print order automatically
        printOrder(saved);

        return mapToDTO(saved);
    }

    /**
     * Print the order on the default system printer
     */
    private void printOrder(Order order) {
        try {
            PrintService printService = PrintServiceLookup.lookupDefaultPrintService();

            if (printService == null) {
                System.err.println("Warning: No default printer found!");
                return;
            }

            String printContent = generatePrintContent(order);

            System.out.println("Printing on: " + printService.getName());
            System.out.println(printContent);

        } catch (Exception e) {
            System.err.println("Error during printing: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Generate the content to print
     */
    private String generatePrintContent(Order order) {
        StringBuilder sb = new StringBuilder();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        LocalDateTime dateTime = LocalDateTime.now();

        sb.append("╔════════════════════════════════════╗\n");
        sb.append("║        COMANDA PRO - ORDER         ║\n");
        sb.append("║                                    ║\n");
        sb.append("╚════════════════════════════════════╝\n\n");

        sb.append("ORDER #").append(order.getId()).append("\n");
        sb.append("Date: ").append(dateTime.format(formatter)).append("\n");
        sb.append("Delivery: ").append(order.getDeliveryTime()).append("\n\n");

        sb.append(" CUSTOMER:\n");
        sb.append("   Name: ").append(order.getCustomer().getName()).append("\n");
        sb.append("   Phone: ").append(order.getCustomer().getPhoneNumber()).append("\n");
        sb.append("   Address: ").append(order.getCustomer().getAddress()).append("\n");
        if (order.getCustomer().getIntercom() != null && !order.getCustomer().getIntercom().isEmpty()) {
            sb.append("   Intercom: ").append(order.getCustomer().getIntercom()).append("\n");
        }
        sb.append("   Zone: ").append(order.getCustomer().getZone()).append("\n\n");

        sb.append(" PRODUCTS:\n");
        sb.append("────────────────────────────────────\n");

        for (OrderLine line : order.getLines()) {
            sb.append("   ").append(line.getProduct().getName()).append("\n");
            sb.append("   Qty: ").append(line.getQuantity());
            sb.append(" x €").append(String.format("%.2f", line.getUnitPrice()));
            sb.append(" = €").append(String.format("%.2f", line.getQuantity() * line.getUnitPrice())).append("\n");

            if (line.getIsPinsa()) {
                sb.append("   ✓ Is Pinsa\n");
            }
            if (line.getNoLactose()) {
                sb.append("   ✓ No Lactose\n");
            }
            if (line.getNotes() != null && !line.getNotes().isEmpty()) {
                sb.append("   Notes: ").append(line.getNotes()).append("\n");
            }
            sb.append("\n");
        }

        sb.append("────────────────────────────────────\n");
        sb.append(" TOTAL: €").append(String.format("%.2f", order.getTotal())).append("\n\n");

        if (order.getNotes() != null && !order.getNotes().isEmpty()) {
            sb.append(" Order Notes:\n");
            sb.append("   ").append(order.getNotes()).append("\n\n");
        }

        sb.append("════════════════════════════════════\n");
        sb.append("          Thank you for your order!\n");
        sb.append("════════════════════════════════════\n");

        return sb.toString();
    }

    public OrderDTO getOrderById(Long id) {
        return orderRepository.findById(id)
            .map(this::mapToDTO)
            .orElseThrow(() -> new RuntimeException("Order not found"));
    }

    public List<OrderDTO> getAllOrders() {
        return orderRepository.findAllOrderByCreatedAtDesc().stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
    }

    public List<OrderDTO> getOrdersByCustomer(Long customerId) {
        return orderRepository.findByCustomerId(customerId).stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
    }

    public List<OrderDTO> getOrdersByStatus(String status) {
        Order.OrderStatus statusEnum = Order.OrderStatus.valueOf(status);
        return orderRepository.findByStatus(statusEnum).stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
    }

    public OrderDTO updateOrderStatus(Long id, String newStatus) {
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Order not found"));

        order.setStatus(Order.OrderStatus.valueOf(newStatus));
        return mapToDTO(orderRepository.save(order));
    }

    public OrderDTO updateOrder(Long id, OrderDTO dto) {
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Order not found"));

        // Update basic fields
        order.setDeliveryTime(dto.getDeliveryTime());
        order.setNotes(dto.getNotes());
        order.setPaymentMethod(dto.getPaymentMethod() != null ? dto.getPaymentMethod() : "CASH");

        // Update status if provided
        if (dto.getStatus() != null) {
            order.setStatus(Order.OrderStatus.valueOf(dto.getStatus()));
        }

        // Update order lines if provided
        if (dto.getLines() != null && !dto.getLines().isEmpty()) {
            // Clear existing lines
            order.getLines().clear();
            order.setCountClassic(0);
            order.setCountPanozzi(0);
            order.setCountRolled(0);
            order.setCountPinse(0);

            // Add new lines
            for (OrderLineDTO lineDTO : dto.getLines()) {
                Product product = productRepository.findById(lineDTO.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found"));

                OrderLine line = OrderLine.builder()
                    .order(order)
                    .product(product)
                    .quantity(lineDTO.getQuantity())
                    .unitPrice(product.getPrice())
                    .notes(lineDTO.getNotes())
                    .isPinsa(lineDTO.getIsPinsa() != null ? lineDTO.getIsPinsa() : false)
                    .noLactose(lineDTO.getNoLactose() != null ? lineDTO.getNoLactose() : false)
                    .build();

                order.getLines().add(line);
                updateCounters(order, product.getCategory(), lineDTO.getQuantity(), lineDTO.getIsPinsa());
            }

            // Recalculate total
            order.calculateTotal();
        }

        return mapToDTO(orderRepository.save(order));
    }

    public void deleteOrder(Long id) {
        orderRepository.deleteById(id);
    }

    public OrderSummaryDTO getOrderSummary() {
        LocalDate today = LocalDate.now();
        long startOfDay = today.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
        long endOfDay = today.atStartOfDay(ZoneId.systemDefault()).plusDays(1).toInstant().toEpochMilli() - 1;

        List<Order> todayOrders = orderRepository.findAll().stream()
            .filter(o -> o.getCreatedAt() >= startOfDay && o.getCreatedAt() <= endOfDay)
            .collect(Collectors.toList());

        return OrderSummaryDTO.builder()
            .totalOrders((long) todayOrders.size())
            .totalRevenue(todayOrders.stream().mapToDouble(Order::getTotal).sum())
            .totalClassic(todayOrders.stream().mapToInt(Order::getCountClassic).sum())
            .totalPanozzi(todayOrders.stream().mapToInt(Order::getCountPanozzi).sum())
            .totalRolled(todayOrders.stream().mapToInt(Order::getCountRolled).sum())
            .totalPinse(todayOrders.stream().mapToInt(Order::getCountPinse).sum())
            .ordersByStatus(Arrays.stream(Order.OrderStatus.values())
                .collect(Collectors.toMap(
                    Enum::name,
                    status -> todayOrders.stream()
                        .filter(o -> o.getStatus() == status)
                        .count()
                )))
            .build();
    }

    public List<DailyOrderSummaryDTO> getOrderSummaryByDateRange(LocalDate startDate, LocalDate endDate) {
        long startOfDay = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
        long endOfDayFinal = endDate.atStartOfDay(ZoneId.systemDefault()).plusDays(1).toInstant().toEpochMilli() - 1;

        List<Order> allOrders = orderRepository.findAll();

        Map<LocalDate, DailyOrderSummaryDTO> dailySummaryMap = new HashMap<>();

        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            long dayStart = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
            long dayEnd = date.atStartOfDay(ZoneId.systemDefault()).plusDays(1).toInstant().toEpochMilli() - 1;

            List<Order> dayOrders = allOrders.stream()
                .filter(o -> o.getCreatedAt() >= dayStart && o.getCreatedAt() <= dayEnd)
                .collect(Collectors.toList());

            DailyOrderSummaryDTO daily = DailyOrderSummaryDTO.builder()
                .date(date)
                .totalOrders((long) dayOrders.size())
                .totalRevenue(dayOrders.stream().mapToDouble(Order::getTotal).sum())
                .totalClassic(dayOrders.stream().mapToInt(Order::getCountClassic).sum())
                .totalPanozzi(dayOrders.stream().mapToInt(Order::getCountPanozzi).sum())
                .totalRolled(dayOrders.stream().mapToInt(Order::getCountRolled).sum())
                .totalPinse(dayOrders.stream().mapToInt(Order::getCountPinse).sum())
                .build();

            dailySummaryMap.put(date, daily);
        }

        return new ArrayList<>(dailySummaryMap.values()).stream()
            .sorted((a, b) -> a.getDate().compareTo(b.getDate()))
            .collect(Collectors.toList());
    }

    private void updateCounters(Order order, Product.Category category, Integer quantity, Boolean isPinsa) {
        if (isPinsa && category == Product.Category.CLASSIC) {
            order.setCountPinse(order.getCountPinse() + quantity);
        } else {
            switch (category) {
                case CLASSIC -> order.setCountClassic(order.getCountClassic() + quantity);
                case PANOZZI -> order.setCountPanozzi(order.getCountPanozzi() + quantity);
                case ROLLED -> order.setCountRolled(order.getCountRolled() + quantity);
                case PINSE -> order.setCountPinse(order.getCountPinse() + quantity);
                default -> {}
            }
        }
    }

    private OrderDTO mapToDTO(Order order) {
        return OrderDTO.builder()
            .id(order.getId())
            .customer(mapCustomerToDTO(order.getCustomer()))
            .lines(order.getLines().stream().map(this::mapLineToDTO).collect(Collectors.toList()))
            .total(order.getTotal())
            .status(order.getStatus().name())
            .deliveryTime(order.getDeliveryTime())
            .notes(order.getNotes())
            .countClassic(order.getCountClassic())
            .countPanozzi(order.getCountPanozzi())
            .countRolled(order.getCountRolled())
            .countPinse(order.getCountPinse())
            .createdAt(order.getCreatedAt())
            .paymentMethod(order.getPaymentMethod())
            .customerDataRemoved(order.getCustomerDataRemoved())
            .build();
    }

    private CustomerDTO mapCustomerToDTO(Customer customer) {
        return CustomerDTO.builder()
            .id(customer.getId())
            .phoneNumber(customer.getPhoneNumber())
            .name(customer.getName())
            .address(customer.getAddress())
            .intercom(customer.getIntercom())
            .zone(customer.getZone())
            .build();
    }

    private OrderLineDTO mapLineToDTO(OrderLine line) {
        return OrderLineDTO.builder()
            .id(line.getId())
            .productId(line.getProduct().getId())
            .productName(line.getProduct().getName())
            .productCategory(line.getProduct().getCategory().name())
            .quantity(line.getQuantity())
            .unitPrice(line.getUnitPrice())
            .subtotal(line.getSubtotal())
            .notes(line.getNotes())
            .isPinsa(line.getIsPinsa())
            .noLactose(line.getNoLactose())
            .build();
    }

}
