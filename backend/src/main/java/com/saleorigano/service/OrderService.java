package com.saleorigano.service;

import com.saleorigano.dto.CustomerDTO;
import com.saleorigano.dto.OrderDTO;
import com.saleorigano.dto.OrderSummaryDTO;
import com.saleorigano.dto.OrderLineDTO;
import com.saleorigano.model.Customer;
import com.saleorigano.model.Order;
import com.saleorigano.model.Product;
import com.saleorigano.model.OrderLine;
import com.saleorigano.repository.CustomerRepository;
import com.saleorigano.repository.OrderRepository;
import com.saleorigano.repository.ProductRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.print.PrintService;
import javax.print.PrintServiceLookup;

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

    public void deleteOrder(Long id) {
        orderRepository.deleteById(id);
    }

    public OrderSummaryDTO getOrderSummary() {
        List<Order> orders = orderRepository.findAll();

        return OrderSummaryDTO.builder()
            .totalOrders((long) orders.size())
            .totalRevenue(orders.stream().mapToDouble(Order::getTotal).sum())
            .totalClassic(orders.stream().mapToInt(Order::getCountClassic).sum())
            .totalPanozzi(orders.stream().mapToInt(Order::getCountPanozzi).sum())
            .totalRolled(orders.stream().mapToInt(Order::getCountRolled).sum())
            .totalPinse(orders.stream().mapToInt(Order::getCountPinse).sum())
            .ordersByStatus(Arrays.stream(Order.OrderStatus.values())
                .collect(Collectors.toMap(
                    Enum::name,
                    status -> orderRepository.countByStatus(status)
                )))
            .build();
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
