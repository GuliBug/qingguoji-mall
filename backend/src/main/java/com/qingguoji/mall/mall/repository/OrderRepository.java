package com.qingguoji.mall.mall.repository;

import com.qingguoji.mall.mall.domain.MallOrder;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 订单数据访问
 *
 * @author qingguoji
 */
@Repository
public interface OrderRepository extends JpaRepository<MallOrder, Long> {

    Page<MallOrder> findByMemberId(Long memberId, Pageable pageable);

    Page<MallOrder> findByMemberIdAndStatus(Long memberId, String status, Pageable pageable);

    Page<MallOrder> findByStatus(String status, Pageable pageable);

    Optional<MallOrder> findByOrderNo(String orderNo);

    /** 超时未支付订单（O-05 定时任务） */
    List<MallOrder> findByStatusAndPayDeadlineBefore(String status, LocalDateTime time);
}
