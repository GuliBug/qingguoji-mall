package com.qingguoji.mall.mall.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 会员（V-01 等级体系：注册 / 铜 / 银 / 金 / 黑金）
 *
 * @author qingguoji
 */
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

    /** 累计成长值（消费 1 元 = 1 成长值，只增不减，退款按比例扣回） */
    private Double growth;

    /** 可用积分 */
    private Integer points;

    private LocalDateTime createdAt;

    /** 各等级成长值门槛，索引即等级值 */
    private static final double[] GROWTH_THRESHOLD = {0, 1000, 5000, 20000, 50000};

    /** 等级积分倍率（V-02） */
    private static final double[] POINT_RATE = {1.0, 1.2, 1.5, 2.0, 2.5};

    /** 等级折扣（V-02） */
    private static final double[] DISCOUNT = {1.0, 1.0, 0.95, 0.90, 0.85};

    /** 等级名称 */
    private static final String[] LEVEL_NAME = {"注册会员", "铜卡", "银卡", "金卡", "黑金卡"};

    /** 等级枚举值：0 注册 1 铜 2 银 3 金 4 黑金 */
    @Transient
    public int level() {
        double growthValue = growth == null ? 0D : growth;
        for (int i = GROWTH_THRESHOLD.length - 1; i > 0; i--) {
            if (growthValue >= GROWTH_THRESHOLD[i]) {
                return i;
            }
        }
        return 0;
    }

    /** 等级倍率（V-02） */
    @Transient
    public double pointRate() {
        return POINT_RATE[level()];
    }

    /** 等级折扣（V-02） */
    @Transient
    public double discount() {
        return DISCOUNT[level()];
    }

    /** 等级名称 */
    @Transient
    public String levelName() {
        return LEVEL_NAME[level()];
    }
}
