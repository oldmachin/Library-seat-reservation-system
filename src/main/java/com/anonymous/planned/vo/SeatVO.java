package com.anonymous.planned.vo;

import java.util.List;

public record SeatVO(
        Long seatId,
        String seatCode,
        Integer xAxis,      // SVG 物理 X 坐标
        Integer yAxis,      // SVG 物理 Y 坐标
        Integer status, // 状态码：0-空闲, 1-占用
        List<String> tags // 特征标签，如 ["window", "power"]
) {}
