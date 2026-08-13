package com.ecommerce.inventory_service.service.impl;

import com.ecommerce.inventory_service.dto.InventoryRequestDto;
import com.ecommerce.inventory_service.dto.InventoryResponseDto;
import com.ecommerce.inventory_service.exception.ResourceNotFoundException;
import com.ecommerce.inventory_service.mapper.InventoryMapper;
import com.ecommerce.inventory_service.model.Inventory;
import com.ecommerce.inventory_service.repository.InventoryRepository;
import com.ecommerce.inventory_service.service.InventoryService;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@RefreshScope
public class InventoryServiceImpl implements InventoryService {

  private final InventoryRepository repository;
  private final InventoryMapper mapper;

  @Value("${inventory.allow-backorders:false}")
  private boolean allowBackorders;

  @Override
  @Transactional(readOnly = true)
  public boolean inInStock(String sku, Integer quantity) {

    if (allowBackorders) {
      log.warn("Backorders mode activated: Authorizing stock for SKU: {}", sku);
      return true;
    }
    return repository
        .findBySku(sku)
        .map(inventory -> inventory.getQuantity() >= quantity)
        .orElse(false);
  }

  @Override
  @Transactional(readOnly = true)
  public List<InventoryResponseDto> getAllInventory() {
    List<Inventory> inventory = repository.findAll();
    return inventory.stream().map(mapper::toInventoryResponseDto).collect(Collectors.toList());
  }

  @Override
  @Transactional
  public InventoryResponseDto createInventory(InventoryRequestDto requestDto) {
    boolean exists = repository.existsBySku(requestDto.getSku());
    if (exists) {
      throw new RuntimeException("Inventory with sku: " + requestDto.getSku() + " already exists");
    }
    Inventory inventory = mapper.toInventory(requestDto);
    Inventory savedInventory = repository.save(inventory);

    log.info("Created inventory with sku: {}", savedInventory.getSku());

    return mapper.toInventoryResponseDto(savedInventory);
  }

  @Override
  @Transactional
  public InventoryResponseDto updateInventory(Long id, InventoryRequestDto requestDto) {

    Inventory inventory =
        repository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Inventory", "id", id));

    mapper.updateInventoryFromRequest(requestDto, inventory);
    Inventory updatedInventory = repository.save(inventory);

    log.info("Updated inventory with id: {}", updatedInventory.getId());

    return mapper.toInventoryResponseDto(updatedInventory);
  }

  @Override
  @Transactional
  public void deleteInventory(Long id) {
    if (!repository.existsById(id)) {
      throw new ResourceNotFoundException("Inventory", "id", id);
    }

    repository.deleteById(id);

    log.info("Deleted inventory with id: {}", id);
  }

  @Override
  @Transactional
  public void reduceStock(String sku, Integer quantity) {

    var inventory =
        repository
            .findBySku(sku)
            .orElseThrow(() -> new ResourceNotFoundException("Inventory", "sku", sku));

    if (inventory.getQuantity() < quantity) {
      throw new RuntimeException("Inventory with sku: " + sku + " does not have enough stock");
    }

    inventory.setQuantity(inventory.getQuantity() - quantity);
    repository.save(inventory);
  }
}
