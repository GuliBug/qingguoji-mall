package com.qingguoji.mall.mall.repository;

import com.qingguoji.mall.mall.domain.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 商品数据访问
 *
 * @author qingguoji
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    /** 按状态分页（客户端只看上架） */
    Page<Product> findByStatus(Integer status, Pageable pageable);

    /** 按系列 + 状态分页 */
    Page<Product> findByCategoryAndStatus(String category, Integer status, Pageable pageable);
}
