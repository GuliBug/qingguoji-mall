package com.qingguoji.mall.mall.repository;

import com.qingguoji.mall.mall.domain.ScanCode;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 防伪码数据访问
 *
 * @author qingguoji
 */
@Repository
public interface ScanCodeRepository extends JpaRepository<ScanCode, Long> {

    Optional<ScanCode> findByCode(String code);
}
