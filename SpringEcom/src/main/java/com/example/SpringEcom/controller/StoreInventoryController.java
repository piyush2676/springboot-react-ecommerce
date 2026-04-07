package com.example.SpringEcom.controller;

import com.example.SpringEcom.model.Store;
import com.example.SpringEcom.model.dto.StoreInventoryResponse;
import com.example.SpringEcom.model.dto.UpdateStoreInventoryRequest;
import com.example.SpringEcom.service.StoreInventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class StoreInventoryController {

    @Autowired
    private StoreInventoryService storeInventoryService;

    /**
     * GET /api/products/{productId}/store-inventory
     * Returns real-time stock levels for a product across all stores.
     * Consumers use this BEFORE leaving home to check availability.
     */
    @GetMapping("/products/{productId}/store-inventory")
    public ResponseEntity<List<StoreInventoryResponse>> getStoreInventory(
            @PathVariable int productId) {
        List<StoreInventoryResponse> inventory = storeInventoryService.getInventoryByProduct(productId);
        return new ResponseEntity<>(inventory, HttpStatus.OK);
    }

    /**
     * GET /api/products/{productId}/store-inventory/in-stock
     * Returns ONLY stores where the product is currently available (stock > 0).
     * Ideal for "Where can I buy this now?" use-case.
     */
    @GetMapping("/products/{productId}/store-inventory/in-stock")
    public ResponseEntity<List<StoreInventoryResponse>> getInStockStores(
            @PathVariable int productId) {
        List<StoreInventoryResponse> inventory = storeInventoryService.getInStockStoresForProduct(productId);
        return new ResponseEntity<>(inventory, HttpStatus.OK);
    }

    /**
     * GET /api/stores
     * Returns all active retail store locations.
     */
    @GetMapping("/stores")
    public ResponseEntity<List<Store>> getAllStores() {
        return new ResponseEntity<>(storeInventoryService.getAllStores(), HttpStatus.OK);
    }

    /**
     * PUT /api/products/{productId}/store-inventory
     * Admin endpoint — updates stock for a product at a given store.
     * Body: { "storeId": 1, "stockQuantity": 25 }
     */
    @PutMapping("/products/{productId}/store-inventory")
    public ResponseEntity<StoreInventoryResponse> updateInventory(
            @PathVariable int productId,
            @RequestBody UpdateStoreInventoryRequest request) {
        StoreInventoryResponse response = storeInventoryService.updateStoreInventory(productId, request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * PUT /api/products/{productId}/store-inventory/{storeId}/deduct
     * Deducts stock at a specific store (called during store-level checkout).
     * Body: { "quantity": 2 }
     */
    @PutMapping("/products/{productId}/store-inventory/{storeId}/deduct")
    public ResponseEntity<String> deductStock(
            @PathVariable int productId,
            @PathVariable Long storeId,
            @RequestParam int quantity) {
        try {
            storeInventoryService.deductStockFromStore(productId, storeId, quantity);
            return new ResponseEntity<>("Stock updated successfully", HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}
