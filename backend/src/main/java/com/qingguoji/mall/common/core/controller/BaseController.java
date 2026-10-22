package com.qingguoji.mall.common.core.controller;

import com.qingguoji.mall.common.core.domain.AjaxResult;
import com.qingguoji.mall.common.core.page.TableDataInfo;
import com.qingguoji.mall.framework.security.LoginHelper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

/**
 * Controller 基类（参考若依 BaseController）
 *
 * <p>收敛响应包装、分页构造、当前登录人获取，业务 Controller 只保留路由与参数。
 *
 * @author qingguoji
 */
public class BaseController {

    /** 默认页码（后端从 0 开始） */
    protected static final int DEFAULT_PAGE = 0;

    /** 默认每页条数 */
    protected static final int DEFAULT_SIZE = 10;

    /** 成功响应（无数据） */
    protected AjaxResult<Void> success() {
        return AjaxResult.success();
    }

    /** 成功响应 */
    protected <T> AjaxResult<T> success(T data) {
        return AjaxResult.success(data);
    }

    /** 成功响应（自定义提示） */
    protected <T> AjaxResult<T> success(String msg, T data) {
        return AjaxResult.success(msg, data);
    }

    /** 失败响应 */
    protected AjaxResult<Void> error(String msg) {
        return AjaxResult.error(msg);
    }

    /** 分页响应用 */
    protected <T> AjaxResult<TableDataInfo<T>> getDataTable(Page<T> page) {
        return AjaxResult.success(TableDataInfo.of(page));
    }

    /** 当前买家会员 ID（拦截器已保证已登录） */
    protected Long getMemberId() {
        return LoginHelper.getMemberId();
    }

    /** 当前管理员账号 */
    protected String getAdminName() {
        return LoginHelper.getAdminName();
    }

    /** 构造分页参数（前端 page/size 从 0 开始，与 Spring Data 一致） */
    protected PageRequest buildPageRequest(
            Integer page, Integer size, String orderBy, Sort.Direction direction) {
        int pageIndex = page == null || page < 0 ? DEFAULT_PAGE : page;
        int pageSize = size == null || size <= 0 ? DEFAULT_SIZE : size;
        return PageRequest.of(pageIndex, pageSize, Sort.by(direction, orderBy));
    }
}
