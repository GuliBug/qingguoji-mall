package com.qingguoji.mall.mall.repository;

import com.qingguoji.mall.mall.domain.Member;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 会员数据访问（若依中对应 SysUserMapper，此处为 Spring Data JPA）
 *
 * @author qingguoji
 */
@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    /** 按手机号查会员（登录时复用账号） */
    Optional<Member> findByPhone(String phone);
}
