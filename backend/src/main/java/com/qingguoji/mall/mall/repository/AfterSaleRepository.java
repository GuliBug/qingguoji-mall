package com.qingguoji.mall.mall.repository;

import com.qingguoji.mall.mall.domain.AfterSale;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 售后单数据访问
 *
 * @author qingguoji
 */
@Repository
public interface AfterSaleRepository extends JpaRepository<AfterSale, Long> {

    Page<AfterSale> findByMemberId(Long memberId, Pageable pageable);

    Page<AfterSale> findByStatus(String status, Pageable pageable);

    List<AfterSale> findByOrderItemId(Long orderItemId);
}
