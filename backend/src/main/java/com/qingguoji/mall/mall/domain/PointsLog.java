package com.qingguoji.mall.mall.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 积分流水（V-07：EARN 消费 / SCAN 扫码 / CHECKIN 签到 / EXCHANGE 兑换 / REFUND_BACK 退款扣回）
 *
 * @author qingguoji
 */
@Data
@Entity
@Table(name = "points_log")
public class PointsLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long memberId;

    /** EARN / SCAN / CHECKIN / EXCHANGE / REFUND_BACK */
    private String type;

    /** 正数获得，负数扣减 */
    private Integer amount;

    private String remark;

    private LocalDateTime createdAt;
}
