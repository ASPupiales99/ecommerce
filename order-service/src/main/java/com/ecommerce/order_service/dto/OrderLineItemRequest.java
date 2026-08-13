package com.ecommerce.order_service.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderLineItemRequest {
  @NotBlank(message = "SKU cannot be blank")
  private String sku;

  @NotNull(message = "Price is mandatory")
  @DecimalMin(value = "0.0", inclusive = false, message = "The price must be greater than 0.0")
  private BigDecimal price;

  @NotNull(message = "Quantity is mandatory")
  @Min(value = 1, message = "The quantity must be greater than or equal to 1")
  private Integer quantity;
}
