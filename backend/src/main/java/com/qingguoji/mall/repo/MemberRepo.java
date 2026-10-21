package com.qingguoji.mall.repo;

import com.qingguoji.mall.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepo extends JpaRepository<Member, Long> {
    Optional<Member> findByPhone(String phone);
}
