package com.ecommerce.inventory_service.mapper;

import com.ecommerce.inventory_service.dto.InventoryRequestDto;
import com.ecommerce.inventory_service.dto.InventoryResponseDto;
import com.ecommerce.inventory_service.model.Inventory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface InventoryMapper {

  Inventory toInventory(InventoryRequestDto requestDto);

  @Mapping(target = "inStock", expression = "java(inventory.getQuantity() > 0)")
  InventoryResponseDto toInventoryResponseDto(Inventory inventory);

  @Mapping(target = "id", ignore = true)
  void updateInventoryFromRequest(
      InventoryRequestDto requestDto, @MappingTarget Inventory inventory);
}
