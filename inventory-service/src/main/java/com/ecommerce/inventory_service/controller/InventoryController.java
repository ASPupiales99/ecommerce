package com.ecommerce.inventory_service.controller;

import com.ecommerce.inventory_service.dto.InventoryRequestDto;
import com.ecommerce.inventory_service.dto.InventoryResponseDto;
import com.ecommerce.inventory_service.service.InventoryService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/inventory")
public class InventoryController {

  private final InventoryService service;

  @GetMapping
  @ResponseStatus(HttpStatus.OK)
  public List<InventoryResponseDto> getAllInventory() {
    return service.getAllInventory();
  }

  @GetMapping("/{sku}")
  @ResponseStatus(HttpStatus.OK)
  public boolean isInStock(@PathVariable String sku, @RequestParam("quantity") Integer quantity) {
    return service.inInStock(sku, quantity);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public InventoryResponseDto createInventory(
      @RequestBody @Valid InventoryRequestDto inventoryRequestDto) {
    return service.createInventory(inventoryRequestDto);
  }

  @PutMapping("/{id}")
  @ResponseStatus(HttpStatus.OK)
  public InventoryResponseDto updateInventory(
      @PathVariable("id") Long id, @RequestBody @Valid InventoryRequestDto inventoryRequestDto) {
    return service.updateInventory(id, inventoryRequestDto);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteInventory(@PathVariable Long id) {
    service.deleteInventory(id);
  }

  @PutMapping("/reduce/{sku}")
  @ResponseStatus(HttpStatus.OK)
  public String reduceStock(@PathVariable String sku, @RequestParam("quantity") Integer quantity) {
    service.reduceStock(sku, quantity);
    return "Stock reduced successfully";
  }
}
