package com.ecommerce.order_service.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderRequest {
  @NotEmpty(message = "The order must contain at least one line item")
  @Valid
  private List<OrderLineItemRequest> orderLineItemList;

  @NotBlank(message = "Email is mandatory")
  @Email(message = "Invalid email format")
  private String email;
}
