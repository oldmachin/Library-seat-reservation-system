package com.anonymous.vo;

import java.util.List;

public record SeatVO(
        Long id,
        Integer row, // 座位行号
        Integer col, // 座位列号
        Integer x,      // SVG 物理 X 坐标
        Integer y,      // SVG 物理 Y 坐标
        Integer status, // 状态码：0-空闲, 1-占用
        String color,   // 状态映射的颜色字符串
        List<String> tags // 特征标签，如 ["window", "power"]
) {}
