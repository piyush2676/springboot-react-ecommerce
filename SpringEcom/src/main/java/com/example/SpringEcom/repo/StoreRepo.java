package com.example.SpringEcom.repo;

import com.example.SpringEcom.model.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StoreRepo extends JpaRepository<Store, Long> {

    List<Store> findByActiveTrue();

    List<Store> findByCityIgnoreCase(String city);
}
