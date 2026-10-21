package com.qingguoji.mall.domain;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/** 防伪码（V-04：每包唯一，扫码 +10 积分，每人每日上限 20 包） */
@Data
@Entity
@Table(name = "scan_code",
        uniqueConstraints = @UniqueConstraint(columnNames = "code"))
public class ScanCode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String code;

    /** 是否已被扫码积分 */
    private Boolean used;

    private Long usedBy;

    private LocalDateTime usedAt;
}
