package com.saleorigano.service;

import com.saleorigano.dto.CustomerDTO;
import com.saleorigano.model.Customer;
import com.saleorigano.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerDTO createCustomer(CustomerDTO dto) {
        if (customerRepository.findByPhoneNumber(dto.getPhoneNumber()).isPresent()) {
            throw new IllegalArgumentException("Customer with this phone number already exists");
        }

        Customer customer = Customer.builder()
            .phoneNumber(dto.getPhoneNumber())
            .name(dto.getName())
            .address(dto.getAddress())
            .intercom(dto.getIntercom())
            .zone(dto.getZone())
            .build();

        Customer saved = customerRepository.save(customer);
        return mapToDTO(saved);
    }

    public CustomerDTO getCustomerById(Long id) {
        return customerRepository.findById(id)
            .map(this::mapToDTO)
            .orElseThrow(() -> new RuntimeException("Customer not found"));
    }

    public CustomerDTO getCustomerByPhoneNumber(String phoneNumber) {
        return customerRepository.findByPhoneNumber(phoneNumber)
            .map(this::mapToDTO)
            .orElseThrow(() -> new RuntimeException("Customer not found"));
    }

    public List<CustomerDTO> getAllCustomers() {
        return customerRepository.findAll().stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
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
