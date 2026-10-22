package com.qingguoji.mall.common.core.page;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import lombok.Data;
import org.springframework.data.domain.Page;

/**
 * 分页返回结构（参考若依 TableDataInfo）
 *
 * <p>前端 {@code unwrapPage()} 同时兼容本结构与 Spring Data 的 {@code content/totalElements}，
 * 因此管理端接口可安全切换到本结构。
 *
 * @param <T> 行数据类型
 * @author qingguoji
 */
@Data
public class TableDataInfo<T> implements Serializable {

    @Serial private static final long serialVersionUID = 1L;

    /** 总记录数 */
    private long total;

    /** 列表数据 */
    private List<T> rows;

    public TableDataInfo() {}

    public TableDataInfo(List<T> rows, long total) {
        this.rows = rows;
        this.total = total;
    }

    /** 由 Spring Data 分页对象转换 */
    public static <T> TableDataInfo<T> of(Page<T> page) {
        if (page == null) {
            return new TableDataInfo<>(List.of(), 0L);
        }
        return new TableDataInfo<>(page.getContent(), page.getTotalElements());
    }
}
