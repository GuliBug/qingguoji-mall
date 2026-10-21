package com.qingguoji.mall.repo;

import com.qingguoji.mall.domain.AfterSale;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AfterSaleRepo extends JpaRepository<AfterSale, Long> {
    Page<AfterSale> findByMemberId(Long memberId, Pageable pageable);
    Page<AfterSale> findByStatus(String status, Pageable pageable);
    List<AfterSale> findByOrderItemId(Long orderItemId);
}
