package com.qingguoji.mall.mall.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 防伪码（V-04：每包唯一，扫码 +10 积分，每人每日上限 20 包）
 *
 * @author qingguoji
 */
@Data
@Entity
@Table(
        name = "scan_code",
        uniqueConstraints = @jakarta.persistence.UniqueConstraint(columnNames = "code"))
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
