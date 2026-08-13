package com.ecommerce.inventory_service.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class InventoryRequestDto {
  @NotBlank(message = "Sku cannot be blank")
  private String sku;

  @Min(value = 0, message = "Quantity cannot be negative")
  private Integer quantity;
}
