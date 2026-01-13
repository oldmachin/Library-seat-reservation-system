package com.anonymous.model;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;

@TableName("t_seats")
public class Seat {
    @TableId
    private Long id;

    private Integer rowIndex;

    private Integer colIndex;

    private Integer status;

    @Version
    private Integer version;

    public Seat() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getRowIndex() {
        return rowIndex;
    }

    public Integer getColIndex() {
        return colIndex;
    }

    public Integer getStatus() {
        return status;
    }

    public Integer getVersion() {
        return version;
    }
}
