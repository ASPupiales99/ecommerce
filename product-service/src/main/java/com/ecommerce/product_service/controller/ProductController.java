package com.ecommerce.product_service.controller;

import com.ecommerce.product_service.dto.ProductRequestDto;
import com.ecommerce.product_service.dto.ProductResponseDto;
import com.ecommerce.product_service.service.ProductService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/product")
@RequiredArgsConstructor
@RefreshScope
public class ProductController {

  private final ProductService productService;

  @Value("${app.maintenance.message: Operating system}")
  private String maintenanceMessage;

  @GetMapping
  @ResponseStatus(HttpStatus.OK)
  public List<ProductResponseDto> getAllProducts(HttpServletResponse response) {
    response.addHeader("X-Maintenance-Message", maintenanceMessage);
    return productService.getAllProducts();
  }

  @GetMapping("/{id}")
  @ResponseStatus(HttpStatus.OK)
  public ProductResponseDto getProductById(@PathVariable String id) {
    return productService.getProductById(id);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public ProductResponseDto createProduct(@RequestBody @Valid ProductRequestDto requestDto) {
    return productService.createProduct(requestDto);
  }

  @PutMapping("/{id}")
  @ResponseStatus(HttpStatus.OK)
  public ProductResponseDto updateProduct(
      @PathVariable String id, @RequestBody @Valid ProductRequestDto requestDto) {
    return productService.updateProduct(id, requestDto);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteProductById(@PathVariable String id) {
    productService.deleteProductById(id);
  }
}
