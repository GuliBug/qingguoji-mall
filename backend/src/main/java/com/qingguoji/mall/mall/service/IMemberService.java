package com.qingguoji.mall.mall.service;

import com.qingguoji.mall.mall.domain.Member;
import com.qingguoji.mall.mall.domain.PointsLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 会员与积分服务（U-01~U-03 / V-01~V-07）
 *
 * @author qingguoji
 */
public interface IMemberService {

    /** U-01 手机号验证码登录，新用户自动注册 */
    Member loginByPhone(String phone, String smsCode);

    /** 按 ID 查会员，不存在抛业务异常 */
    Member selectMemberById(Long memberId);

    /** V-07 积分明细 */
    Page<PointsLog> selectPointsLogPage(Long memberId, Pageable pageable);

    /** V-05 每日签到，返回签到后积分 */
    int checkin(Long memberId);

    /** V-04 扫码积分，返回扫码后积分 */
    int scanCode(Long memberId, String code);

    /** 积分增减（统一写流水） */
    int addPoints(Long memberId, String type, int amount, String remark);

    /** M-07 管理端会员列表 */
    Page<Member> selectMemberPage(Pageable pageable);

    /** M-07 人工调整积分，返回调整后积分 */
    int adjustPoints(Long memberId, Integer delta, String reason);
}
