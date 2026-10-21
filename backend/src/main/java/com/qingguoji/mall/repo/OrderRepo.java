package com.qingguoji.mall.repo;

import com.qingguoji.mall.domain.MallOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderRepo extends JpaRepository<MallOrder, Long> {
    Page<MallOrder> findByMemberId(Long memberId, Pageable pageable);
    Page<MallOrder> findByMemberIdAndStatus(Long memberId, String status, Pageable pageable);
    Page<MallOrder> findByStatus(String status, Pageable pageable);
    Optional<MallOrder> findByOrderNo(String orderNo);
    List<MallOrder> findByStatusAndPayDeadlineBefore(String status, LocalDateTime time);
}
