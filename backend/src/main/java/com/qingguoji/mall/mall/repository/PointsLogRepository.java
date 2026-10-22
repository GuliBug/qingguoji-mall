package com.qingguoji.mall.mall.repository;

import com.qingguoji.mall.mall.domain.PointsLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 积分流水数据访问
 *
 * @author qingguoji
 */
@Repository
public interface PointsLogRepository extends JpaRepository<PointsLog, Long> {

    Page<PointsLog> findByMemberId(Long memberId, Pageable pageable);
}
