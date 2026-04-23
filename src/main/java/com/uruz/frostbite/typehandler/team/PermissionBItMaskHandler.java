package com.uruz.frostbite.typehandler.team;

import com.uruz.frostbite.enums.PermissionBitMask;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@MappedTypes(PermissionBitMask.class)
@MappedJdbcTypes(value = JdbcType.SMALLINT)
public class PermissionBItMaskHandler extends BaseTypeHandler<PermissionBitMask> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, PermissionBitMask parameter, JdbcType jdbcType) throws SQLException {
        ps.setInt(i, parameter.getValue());
    }

    @Override
    public PermissionBitMask getNullableResult(ResultSet rs, String columnName) throws SQLException {
        int value = rs.getInt(columnName);
        return PermissionBitMask.fromValue(value);
    }

    @Override
    public PermissionBitMask getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        int value = rs.getInt(columnIndex);
        return PermissionBitMask.fromValue(value);
    }

    @Override
    public PermissionBitMask getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        int value = cs.getInt(columnIndex);
        return PermissionBitMask.fromValue(value);
    }
}
