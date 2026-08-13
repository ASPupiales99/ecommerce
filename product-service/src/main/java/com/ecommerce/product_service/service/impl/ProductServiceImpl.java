package com.ecommerce.product_service.service.impl;

import com.ecommerce.product_service.dto.ProductRequestDto;
import com.ecommerce.product_service.dto.ProductResponseDto;
import com.ecommerce.product_service.exception.ResourceNotFoundException;
import com.ecommerce.product_service.mapper.ProductMapper;
import com.ecommerce.product_service.model.Product;
import com.ecommerce.product_service.repository.ProductRepository;
import com.ecommerce.product_service.service.ProductService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService {

  private final ProductRepository productRepository;
  private final ProductMapper mapper;

  @Override
  public ProductResponseDto createProduct(ProductRequestDto requestDto) {
    Product product = mapper.toProduct(requestDto);
    Product savedProduct = productRepository.save(product);

    log.info("Product '{}' created", savedProduct.getName());

    return mapper.toProductResponseDto(savedProduct);
  }

  @Override
  public List<ProductResponseDto> getAllProducts() {
    List<Product> products = productRepository.findAll();
    return products.stream().map(mapper::toProductResponseDto).toList();
  }

  @Override
  public ProductResponseDto getProductById(String id) {
    Product product =
        productRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
    return mapper.toProductResponseDto(product);
  }

  @Override
  public ProductResponseDto updateProduct(String id, ProductRequestDto requestDto) {
    Product product =
        productRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
    mapper.updateProductFromRequest(requestDto, product);
    Product updatedProduct = productRepository.save(product);

    log.info("Product '{}' updated", updatedProduct.getName());

    return mapper.toProductResponseDto(updatedProduct);
  }

  @Override
  public void deleteProductById(String id) {

    if (!productRepository.existsById(id)) {
      throw new ResourceNotFoundException("Product", "id", id);
    }

    productRepository.deleteById(id);

    log.info("Product with id: '{}' deleted", id);
  }
}
