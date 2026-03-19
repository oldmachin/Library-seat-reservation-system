package com.anonymous.handler;

import com.anonymous.model.enums.SeatStatus;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@MappedTypes(SeatStatus.class)
public class SeatStatusHandler extends BaseTypeHandler<SeatStatus> {
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, SeatStatus parameter, JdbcType jdbcType) throws SQLException {
        ps.setInt(i, parameter.getCode());
    }

    @Override
    public SeatStatus getNullableResult(ResultSet rs, String columnName) throws SQLException {
        int code = rs.getInt(columnName);
        return rs.wasNull() ? null : SeatStatus.fromCode(code);
    }

    @Override
    public SeatStatus getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        int code = rs.getInt(columnIndex);
        return rs.wasNull() ? null : SeatStatus.fromCode(code);
    }

    @Override
    public SeatStatus getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        int code = cs.getInt(columnIndex);
        return cs.wasNull() ? null : SeatStatus.fromCode(code);
    }
}
