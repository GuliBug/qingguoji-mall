package com.qingguoji.mall.common;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/** 分页结果（参考若依 TableDataInfo） */
@Data
@AllArgsConstructor
public class PageResult<T> {
    private long total;
    private List<T> rows;
}
