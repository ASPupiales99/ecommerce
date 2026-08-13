package com.ecommerce.product_service.dataloader;

import com.ecommerce.product_service.model.Product;
import com.ecommerce.product_service.repository.ProductRepository;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TestDataLoader implements CommandLineRunner {
  private final ProductRepository productRepository;

  @Override
  public void run(String @NonNull ... args) throws Exception {
    Product product =
        Product.builder()
            .name("Samsung Galaxy")
            .description("Description telefono")
            .price(BigDecimal.valueOf(1200))
            .build();

    productRepository.save(product);

    System.out.print("Producto creado exitosamente");
  }
}
