package com.qingguoji.mall.repo;

import com.qingguoji.mall.domain.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartRepo extends JpaRepository<CartItem, Long> {
    List<CartItem> findByMemberId(Long memberId);
    Optional<CartItem> findByMemberIdAndSkuId(Long memberId, Long skuId);
}
