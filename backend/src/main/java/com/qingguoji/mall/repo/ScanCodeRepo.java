package com.qingguoji.mall.repo;

import com.qingguoji.mall.domain.ScanCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ScanCodeRepo extends JpaRepository<ScanCode, Long> {
    Optional<ScanCode> findByCode(String code);
}
