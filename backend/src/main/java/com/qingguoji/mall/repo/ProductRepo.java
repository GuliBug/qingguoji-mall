package com.qingguoji.mall.repo;

import com.qingguoji.mall.domain.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepo extends JpaRepository<Product, Long> {
    Page<Product> findByStatus(Integer status, Pageable pageable);
    Page<Product> findByCategoryAndStatus(String category, Integer status, Pageable pageable);
}
