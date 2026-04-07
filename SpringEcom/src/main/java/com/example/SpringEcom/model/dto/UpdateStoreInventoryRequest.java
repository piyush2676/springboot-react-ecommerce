package com.example.SpringEcom.model.dto;

public record UpdateStoreInventoryRequest(
        Long storeId,
        int stockQuantity
) {}
