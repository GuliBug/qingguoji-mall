package com.qingguoji.mall.domain;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/** 积分流水（V-07：EARN消费/SCAN扫码/CHECKIN签到/EXCHANGE兑换/REFUND_BACK退款扣回） */
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
