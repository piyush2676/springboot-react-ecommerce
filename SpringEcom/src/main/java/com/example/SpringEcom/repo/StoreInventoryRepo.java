package com.example.SpringEcom.repo;

import com.example.SpringEcom.model.StoreInventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface StoreInventoryRepo extends JpaRepository<StoreInventory, Long> {

    List<StoreInventory> findByProductId(int productId);

    Optional<StoreInventory> findByProductIdAndStoreId(int productId, Long storeId);

    @Query("SELECT si FROM StoreInventory si JOIN FETCH si.store s JOIN FETCH si.product p " +
           "WHERE p.id = :productId AND s.active = true ORDER BY si.stockQuantity DESC")
    List<StoreInventory> findAvailableStoresForProduct(@Param("productId") int productId);

    @Query("SELECT si FROM StoreInventory si JOIN FETCH si.store s JOIN FETCH si.product p " +
           "WHERE p.id = :productId AND si.stockQuantity > 0 AND s.active = true")
    List<StoreInventory> findInStockStoresForProduct(@Param("productId") int productId);
}
