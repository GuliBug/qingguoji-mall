package com.qingguoji.mall.mall.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 商品（P-01~P-06：槟榔产品，系列 / 上下架 / 销量）
 *
 * @author qingguoji
 */
@Data
@Entity
@Table(name = "product")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    /** 系列：枸杞系列 / 咖啡系列 / 冰榔系列 / 礼盒 / 经典系列 */
    private String category;

    /** 副标题卖点 */
    private String subtitle;

    /** 封面色值（原型演示用） */
    private String cover;

    private Integer sales;

    /** 1 上架 0 下架 */
    private Integer status;

    /** 积分标记：NORMAL / NORMAL2X */
    private String pointsFlag;

    private LocalDateTime createdAt;
}
