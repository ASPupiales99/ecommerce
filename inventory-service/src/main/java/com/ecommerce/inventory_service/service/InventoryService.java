package com.ecommerce.inventory_service.service;

import com.ecommerce.inventory_service.dto.InventoryRequestDto;
import com.ecommerce.inventory_service.dto.InventoryResponseDto;
import java.util.List;

public interface InventoryService {
  boolean inInStock(String sku, Integer quantity);

  List<InventoryResponseDto> getAllInventory();

  InventoryResponseDto createInventory(InventoryRequestDto requestDto);

  InventoryResponseDto updateInventory(Long id, InventoryRequestDto requestDto);

  void deleteInventory(Long id);

  void reduceStock(String sku, Integer quantity);
}
