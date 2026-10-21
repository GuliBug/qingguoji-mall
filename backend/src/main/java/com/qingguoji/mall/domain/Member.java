package com.qingguoji.mall.domain;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/** 会员（V-01 等级体系：注册/铜/银/金/黑金） */
@Data
@Entity
@Table(name = "member")
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String phone;

    private String nickname;

    /** 累计成长值（消费1元=1成长值，只增不减，退款按比例扣回） */
    private Double growth;

    /** 可用积分 */
    private Integer points;

    private LocalDateTime createdAt;

    /** 等级枚举值：0注册 1铜 2银 3金 4黑金 */
    @Transient
    public int level() {
        if (growth >= 50000) return 4;
        if (growth >= 20000) return 3;
        if (growth >= 5000) return 2;
        if (growth >= 1000) return 1;
        return 0;
    }

    private static final double[] POINT_RATE = {1.0, 1.2, 1.5, 2.0, 2.5};
    private static final double[] DISCOUNT   = {1.0, 1.0, 0.95, 0.90, 0.85};
    private static final String[] LEVEL_NAME = {"注册会员", "铜卡", "银卡", "金卡", "黑金卡"};

    /** 等级倍率（V-02） */
    @Transient
    public double pointRate() { return POINT_RATE[level()]; }

    /** 等级折扣（V-02） */
    @Transient
    public double discount() { return DISCOUNT[level()]; }

    @Transient
    public String levelName() { return LEVEL_NAME[level()]; }
}
