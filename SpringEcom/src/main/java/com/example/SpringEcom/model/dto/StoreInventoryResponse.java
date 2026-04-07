package com.example.SpringEcom.model.dto;

import java.time.LocalDateTime;

public record StoreInventoryResponse(
        Long storeId,
        String storeName,
        String storeAddress,
        String city,
        String pincode,
        double latitude,
        double longitude,
        int stockQuantity,
        boolean inStock,
        LocalDateTime lastUpdated
) {}
