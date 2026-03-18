package com.saleorigano.service;

import com.saleorigano.dto.ProductDTO;
import com.saleorigano.model.Product;
import com.saleorigano.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    public ProductDTO createProduct(ProductDTO dto) {
        Product.Category category = Product.Category.valueOf(dto.getCategory());

        if (productRepository.findByNameAndCategory(dto.getName(), category).isPresent()) {
            throw new IllegalArgumentException("Product with this name in this category already exists");
        }

        Product product = Product.builder()
            .name(dto.getName())
            .price(dto.getPrice())
            .category(category)
            .available(dto.getAvailable() != null ? dto.getAvailable() : true)
            .build();

        return mapToDTO(productRepository.save(product));
    }

    public ProductDTO getProductById(Long id) {
        return productRepository.findById(id)
            .map(this::mapToDTO)
            .orElseThrow(() -> new RuntimeException("Product not found"));
    }

    public List<ProductDTO> getProductsByCategory(String category) {
        Product.Category cat = Product.Category.valueOf(category);
        return productRepository.findByCategory(cat).stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
    }

    public List<ProductDTO> getAllProducts() {
        return productRepository.findAll().stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
    }

    public ProductDTO updateProduct(Long id, ProductDTO dto) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Product not found"));

        product.setName(dto.getName());
        product.setPrice(dto.getPrice());
        product.setAvailable(dto.getAvailable());

        return mapToDTO(productRepository.save(product));
    }

    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }

    private ProductDTO mapToDTO(Product product) {
        return ProductDTO.builder()
            .id(product.getId())
            .name(product.getName())
            .price(product.getPrice())
            .category(product.getCategory().name())
            .available(product.getAvailable())
            .build();
    }

}
