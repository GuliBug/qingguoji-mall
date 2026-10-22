package com.qingguoji.mall.mall.service.impl;

import com.qingguoji.mall.common.constant.Constants;
import com.qingguoji.mall.common.exception.ServiceException;
import com.qingguoji.mall.common.utils.StringUtils;
import com.qingguoji.mall.mall.domain.Member;
import com.qingguoji.mall.mall.domain.PointsLog;
import com.qingguoji.mall.mall.domain.ScanCode;
import com.qingguoji.mall.mall.repository.MemberRepository;
import com.qingguoji.mall.mall.repository.PointsLogRepository;
import com.qingguoji.mall.mall.repository.ScanCodeRepository;
import com.qingguoji.mall.mall.service.IMemberService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 会员与积分服务实现
 *
 * @author qingguoji
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements IMemberService {

    /** 扫码每日上限（AC-10） */
    private static final int SCAN_DAILY_LIMIT = 20;

    /** 每次扫码积分 */
    private static final int SCAN_POINTS = 10;

    /** 签到积分 */
    private static final int CHECKIN_POINTS = 5;

    /** 签到 / 扫码判重时回看最近流水条数 */
    private static final int RECENT_LOG_SIZE = 100;

    private final MemberRepository memberRepository;
    private final PointsLogRepository pointsLogRepository;
    private final ScanCodeRepository scanCodeRepository;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Member loginByPhone(String phone, String smsCode) {
        if (!StringUtils.isPhone(phone)) {
            throw new ServiceException("手机号格式不正确（需 11 位，1 开头）");
        }
        if (StringUtils.isEmpty(smsCode)) {
            throw new ServiceException("验证码不能为空");
        }
        if (!Constants.DEMO_SMS_CODE.equals(smsCode)) {
            throw new ServiceException("验证码错误（演示环境固定为 123456）");
        }
        return memberRepository
                .findByPhone(phone)
                .orElseGet(
                        () -> {
                            Member member = new Member();
                            member.setPhone(phone);
                            member.setNickname("会员" + phone.substring(phone.length() - 4));
                            member.setGrowth(0.0);
                            member.setPoints(0);
                            member.setCreatedAt(LocalDateTime.now());
                            return memberRepository.save(member);
                        });
    }

    @Override
    public Member selectMemberById(Long memberId) {
        return memberRepository.findById(memberId).orElseThrow(() -> new ServiceException("会员不存在"));
    }

    @Override
    public Page<PointsLog> selectPointsLogPage(Long memberId, Pageable pageable) {
        return pointsLogRepository.findByMemberId(memberId, pageable);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int checkin(Long memberId) {
        LocalDate today = LocalDate.now();
        boolean checkedIn =
                recentLogs(memberId).stream()
                        .anyMatch(
                                item ->
                                        "CHECKIN".equals(item.getType())
                                                && item.getCreatedAt() != null
                                                && item.getCreatedAt().toLocalDate().equals(today));
        if (checkedIn) {
            throw new ServiceException("今日已签到");
        }
        return addPoints(memberId, "CHECKIN", CHECKIN_POINTS, "每日签到");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int scanCode(Long memberId, String code) {
        if (StringUtils.isEmpty(code)) {
            throw new ServiceException("防伪码不能为空");
        }
        ScanCode scanCode =
                scanCodeRepository
                        .findByCode(code)
                        .orElseThrow(() -> new ServiceException("防伪码不存在，请核对"));
        if (Boolean.TRUE.equals(scanCode.getUsed())) {
            throw new ServiceException("该码已被使用");
        }

        LocalDate today = LocalDate.now();
        long todayScans =
                recentLogs(memberId).stream()
                        .filter(
                                item ->
                                        "SCAN".equals(item.getType())
                                                && item.getCreatedAt() != null
                                                && item.getCreatedAt().toLocalDate().equals(today))
                        .count();
        if (todayScans >= SCAN_DAILY_LIMIT) {
            throw new ServiceException("今日扫码已达上限（20 包）");
        }

        scanCode.setUsed(true);
        scanCode.setUsedBy(memberId);
        scanCode.setUsedAt(LocalDateTime.now());
        scanCodeRepository.save(scanCode);
        return addPoints(memberId, "SCAN", SCAN_POINTS, "扫码积分 " + code);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int addPoints(Long memberId, String type, int amount, String remark) {
        Member member = selectMemberById(memberId);
        member.setPoints(member.getPoints() + amount);
        memberRepository.save(member);

        PointsLog pointsLog = new PointsLog();
        pointsLog.setMemberId(memberId);
        pointsLog.setType(type);
        pointsLog.setAmount(amount);
        pointsLog.setRemark(remark);
        pointsLog.setCreatedAt(LocalDateTime.now());
        pointsLogRepository.save(pointsLog);
        return member.getPoints();
    }

    @Override
    public Page<Member> selectMemberPage(Pageable pageable) {
        return memberRepository.findAll(pageable);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int adjustPoints(Long memberId, Integer delta, String reason) {
        if (delta == null) {
            throw new ServiceException("调整数量不能为空");
        }
        if (delta == 0) {
            throw new ServiceException("调整数量不能为 0");
        }
        String type = delta > 0 ? "EARN" : "REFUND_BACK";
        return addPoints(
                memberId, type, delta, "管理员调整: " + StringUtils.defaultIfBlank(reason, "人工调整"));
    }

    /** 取会员最近积分流水（签到 / 扫码判重使用） */
    private java.util.List<PointsLog> recentLogs(Long memberId) {
        Pageable recent =
                PageRequest.of(0, RECENT_LOG_SIZE, Sort.by(Sort.Direction.DESC, "createdAt"));
        return pointsLogRepository.findByMemberId(memberId, recent).getContent();
    }
}
