package com.qingguoji.mall.domain;

import jakarta.persistence.*;
import lombok.Data;

/** 购物车（C-01~C-05，登录用户服务端存储） */
@Data
@Entity
@Table(name = "cart_item",
        uniqueConstraints = @UniqueConstraint(columnNames = {"memberId", "skuId"}))
public class CartItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long memberId;

    private Long skuId;

    private Integer qty;
}
