package com.qingguoji.mall.domain;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/** 商品（P-01~P-06：槟榔产品，系列/上下架/销量） */
@Data
@Entity
@Table(name = "product")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    /** 系列：枸杞系列/咖啡系列/冰榔系列/礼盒/经典系列 */
    private String category;

    /** 副标题卖点 */
    private String subtitle;

    /** 封面色值（原型演示用） */
    private String cover;

    private Integer sales;

    /** 1上架 0下架 */
    private Integer status;

    /** 积分标记：NORMAL/NORMAL2X */
    private String pointsFlag;

    private LocalDateTime createdAt;
}
