package com.qingguoji.mall.repo;

import com.qingguoji.mall.domain.ProductSku;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductSkuRepo extends JpaRepository<ProductSku, Long> {
    List<ProductSku> findByProductId(Long productId);

    /** S-02 原子预占：可售库存足够才锁定（防超卖兜底，S-05） */
    @Modifying
    @Query("UPDATE ProductSku s SET s.lockedStock = s.lockedStock + :qty " +
            "WHERE s.id = :skuId AND (s.totalStock - s.lockedStock) >= :qty")
    int lockStock(@Param("skuId") Long skuId, @Param("qty") int qty);

    /** S-03 支付确认：锁定转实际扣减 */
    @Modifying
    @Query("UPDATE ProductSku s SET s.totalStock = s.totalStock - :qty, s.lockedStock = s.lockedStock - :qty " +
            "WHERE s.id = :skuId AND s.lockedStock >= :qty")
    int deductStock(@Param("skuId") Long skuId, @Param("qty") int qty);

    /** S-04 释放预占 */
    @Modifying
    @Query("UPDATE ProductSku s SET s.lockedStock = s.lockedStock - :qty " +
            "WHERE s.id = :skuId AND s.lockedStock >= :qty")
    int releaseStock(@Param("skuId") Long skuId, @Param("qty") int qty);

    /** S-07 售后回补 */
    @Modifying
    @Query("UPDATE ProductSku s SET s.totalStock = s.totalStock + :qty " +
            "WHERE s.id = :skuId")
    int restock(@Param("skuId") Long skuId, @Param("qty") int qty);
}
