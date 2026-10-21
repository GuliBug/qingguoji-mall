package com.qingguoji.mall.repo;

import com.qingguoji.mall.domain.PointsLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PointsLogRepo extends JpaRepository<PointsLog, Long> {
    Page<PointsLog> findByMemberId(Long memberId, Pageable pageable);
}
