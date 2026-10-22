package com.qingguoji.mall.mall.repository;

import com.qingguoji.mall.mall.domain.CartItem;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 购物车数据访问
 *
 * @author qingguoji
 */
@Repository
public interface CartRepository extends JpaRepository<CartItem, Long> {

    List<CartItem> findByMemberId(Long memberId);

    Optional<CartItem> findByMemberIdAndSkuId(Long memberId, Long skuId);
}
