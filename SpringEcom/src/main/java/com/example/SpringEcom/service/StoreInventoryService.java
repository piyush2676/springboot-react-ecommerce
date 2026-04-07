package com.example.SpringEcom.service;

import com.example.SpringEcom.model.Product;
import com.example.SpringEcom.model.Store;
import com.example.SpringEcom.model.StoreInventory;
import com.example.SpringEcom.model.dto.StoreInventoryResponse;
import com.example.SpringEcom.model.dto.UpdateStoreInventoryRequest;
import com.example.SpringEcom.repo.ProductRepo;
import com.example.SpringEcom.repo.StoreInventoryRepo;
import com.example.SpringEcom.repo.StoreRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class StoreInventoryService {

    @Autowired
    private StoreInventoryRepo storeInventoryRepo;

    @Autowired
    private StoreRepo storeRepo;

    @Autowired
    private ProductRepo productRepo;

    /**
     * Get real-time inventory status for a product across ALL stores.
     */
    public List<StoreInventoryResponse> getInventoryByProduct(int productId) {
        List<StoreInventory> inventories = storeInventoryRepo.findAvailableStoresForProduct(productId);
        return inventories.stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Get ONLY stores where the product is currently in stock (stockQuantity > 0).
     */
    public List<StoreInventoryResponse> getInStockStoresForProduct(int productId) {
        List<StoreInventory> inventories = storeInventoryRepo.findInStockStoresForProduct(productId);
        return inventories.stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Get all active stores.
     */
    public List<Store> getAllStores() {
        return storeRepo.findByActiveTrue();
    }

    /**
     * Update (or create) stock quantity for a specific product at a specific store.
     */
    @Transactional
    public StoreInventoryResponse updateStoreInventory(int productId, UpdateStoreInventoryRequest request) {
        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found: " + productId));

        Store store = storeRepo.findById(request.storeId())
                .orElseThrow(() -> new RuntimeException("Store not found: " + request.storeId()));

        Optional<StoreInventory> existing = storeInventoryRepo.findByProductIdAndStoreId(productId, request.storeId());

        StoreInventory inventory;
        if (existing.isPresent()) {
            inventory = existing.get();
            inventory.setStockQuantity(request.stockQuantity());
        } else {
            inventory = StoreInventory.builder()
                    .product(product)
                    .store(store)
                    .stockQuantity(request.stockQuantity())
                    .build();
        }

        StoreInventory saved = storeInventoryRepo.save(inventory);

        // Also update the aggregate stock on the Product itself
        int totalStock = storeInventoryRepo.findByProductId(productId)
                .stream()
                .mapToInt(StoreInventory::getStockQuantity)
                .sum();
        product.setStockQuantity(totalStock);
        product.setProductAvailable(totalStock > 0);
        productRepo.save(product);

        return toResponse(saved);
    }

    /**
     * Deduct stock from a specific store when an order is placed (store-level checkout).
     */
    @Transactional
    public void deductStockFromStore(int productId, Long storeId, int quantity) {
        StoreInventory inventory = storeInventoryRepo.findByProductIdAndStoreId(productId, storeId)
                .orElseThrow(() -> new RuntimeException("No inventory record for product " + productId + " at store " + storeId));

        if (inventory.getStockQuantity() < quantity) {
            throw new RuntimeException("Insufficient stock at store " + storeId + " for product " + productId);
        }

        inventory.setStockQuantity(inventory.getStockQuantity() - quantity);
        storeInventoryRepo.save(inventory);

        // Sync aggregate product stock
        int totalStock = storeInventoryRepo.findByProductId(productId)
                .stream()
                .mapToInt(StoreInventory::getStockQuantity)
                .sum();
        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        product.setStockQuantity(totalStock);
        product.setProductAvailable(totalStock > 0);
        productRepo.save(product);
    }

    // ─── Mapper ───────────────────────────────────────────────────────────────

    private StoreInventoryResponse toResponse(StoreInventory inv) {
        Store s = inv.getStore();
        return new StoreInventoryResponse(
                s.getId(),
                s.getName(),
                s.getAddress(),
                s.getCity(),
                s.getPincode(),
                s.getLatitude(),
                s.getLongitude(),
                inv.getStockQuantity(),
                inv.getStockQuantity() > 0,
                inv.getLastUpdated()
        );
    }
}
