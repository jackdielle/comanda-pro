package com.saleorigano.service;

import com.saleorigano.dto.CustomerDTO;
import com.saleorigano.model.Customer;
import com.saleorigano.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerService {
    private final CustomerRepository customerRepository;

    public CustomerDTO createCustomer(CustomerDTO dto) {
        log.info("=== CREATE CUSTOMER === Name: {}, Phone: {}", dto.getName(), dto.getPhoneNumber());
        log.debug("Step 1: Checking if customer with phone number already exists");

        if (customerRepository.findByPhoneNumber(dto.getPhoneNumber()).isPresent()) {
            log.error("Customer already exists with phone: {}", dto.getPhoneNumber());
            throw new IllegalArgumentException("Customer with this phone number already exists");
        }
        log.debug("Step 2: Phone number is unique");

        log.debug("Step 3: Building customer entity");
        Customer customer = Customer.builder()
            .phoneNumber(dto.getPhoneNumber())
            .name(dto.getName())
            .address(dto.getAddress())
            .intercom(dto.getIntercom())
            .zone(dto.getZone())
            .build();

        log.debug("Step 4: Saving customer to database");
        Customer saved = customerRepository.save(customer);
        log.info("=== CUSTOMER CREATED === ID: {}, Name: {}", saved.getId(), saved.getName());
        return mapToDTO(saved);
    }

    public CustomerDTO getCustomerById(Long id) {
        log.debug("Getting customer by ID: {}", id);
        return customerRepository.findById(id)
            .map(customer -> {
                log.info("Customer found - ID: {}, Name: {}", customer.getId(), customer.getName());
                return mapToDTO(customer);
            })
            .orElseThrow(() -> {
                log.error("Customer not found with ID: {}", id);
                return new RuntimeException("Customer not found");
            });
    }

    public CustomerDTO getCustomerByPhoneNumber(String phoneNumber) {
        log.debug("Getting customer by phone number: {}", phoneNumber);
        return customerRepository.findByPhoneNumber(phoneNumber)
            .map(customer -> {
                log.info("Customer found - Phone: {}, Name: {}", phoneNumber, customer.getName());
                return mapToDTO(customer);
            })
            .orElseThrow(() -> {
                log.error("Customer not found with phone: {}", phoneNumber);
                return new RuntimeException("Customer not found");
            });
    }

    public List<CustomerDTO> getAllCustomers() {
        log.info("Fetching all customers from database");
        List<CustomerDTO> customers = customerRepository.findAll().stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
        log.info("Retrieved {} customers from database", customers.size());
        return customers;
    }

    public List<CustomerDTO> searchCustomerByName(String name) {
        return customerRepository.findByNameContainingIgnoreCase(name).stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
    }

    public CustomerDTO updateCustomer(Long id, CustomerDTO dto) {
        Customer customer = customerRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Customer not found"));

        customer.setName(dto.getName());
        customer.setAddress(dto.getAddress());
        customer.setIntercom(dto.getIntercom());
        customer.setZone(dto.getZone());

        return mapToDTO(customerRepository.save(customer));
    }

    public void deleteCustomer(Long id) {
        customerRepository.deleteById(id);
    }

    private CustomerDTO mapToDTO(Customer customer) {
        return CustomerDTO.builder()
            .id(customer.getId())
            .phoneNumber(customer.getPhoneNumber())
            .name(customer.getName())
            .address(customer.getAddress())
            .intercom(customer.getIntercom())
            .zone(customer.getZone())
            .build();
    }

}
