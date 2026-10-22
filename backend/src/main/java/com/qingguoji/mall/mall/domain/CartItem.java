package com.qingguoji.mall.mall.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

/**
 * 购物车（C-01~C-05，登录用户服务端存储）
 *
 * @author qingguoji
 */
@Data
@Entity
@Table(
        name = "cart_item",
        uniqueConstraints =
                @jakarta.persistence.UniqueConstraint(columnNames = {"memberId", "skuId"}))
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long memberId;

    private Long skuId;

    private Integer qty;
}
