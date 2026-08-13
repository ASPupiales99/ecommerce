package com.ecommerce.order_service.controller;

import com.ecommerce.order_service.dto.OrderRequest;
import com.ecommerce.order_service.dto.OrderResponse;
import com.ecommerce.order_service.service.OrderService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/order")
@RequiredArgsConstructor
public class OrderController {

  private final OrderService service;

  @GetMapping
  @ResponseStatus(HttpStatus.OK)
  public List<OrderResponse> getOrderHistory(@AuthenticationPrincipal Jwt jwt) {
    String userId = jwt.getSubject();
    boolean isAdmin = false;

    Map<String, Object> realmAccess = jwt.getClaim("realm_access");

    if (realmAccess != null && realmAccess.containsKey("roles")) {
      List<String> roles = (List<String>) realmAccess.get("roles");

      isAdmin = roles.stream().anyMatch(role -> role.equalsIgnoreCase("ADMIN"));
    }

    return service.getOrderHistory(userId, isAdmin);
  }

  @GetMapping("/{id}")
  @ResponseStatus(HttpStatus.OK)
  public OrderResponse getOrderById(@PathVariable Long id) {
    return service.findById(id);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public OrderResponse placeOrder(
      @RequestBody @Valid OrderRequest orderRequest, @AuthenticationPrincipal Jwt jwt) {
    return service.placeOrder(orderRequest, jwt.getSubject());
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteOrder(@PathVariable Long id) {
    service.delete(id);
  }
}
