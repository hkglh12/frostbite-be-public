package com.uruz.frostbite.typehandler.common;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import java.nio.ByteBuffer;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

//@MappedTypes(UUID.class)
//@MappedJdbcTypes(value = JdbcType.VARCHAR)

// TODO DB Schema 자체가 UUID일경우, 오히려 Varchar로 넘기면 안된다.
/*
public class UUIDTypeHandler extends BaseTypeHandler<UUID> {
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, UUID parameter, JdbcType jdbcType) throws SQLException {
        ps.setString(i, parameter.toString());
    }

    @Override
    public UUID getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String value = rs.getString(columnName);
        return toUuid(value);
    }

    @Override
    public UUID getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String value = rs.getString(columnIndex);
        return toUuid(value);
    }

    @Override
    public UUID getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String value = cs.getString(columnIndex);
        return toUuid(value);
    }

    private UUID toUuid(String value) throws SQLException {
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException e) {
            throw new SQLException("Invalid UUID value: " + value, e);
        }
    }
}
*/
@MappedTypes(UUID.class)
@MappedJdbcTypes(JdbcType.OTHER)
public class UUIDTypeHandler extends BaseTypeHandler<UUID> {

    @Override
    public void setNonNullParameter(
            PreparedStatement ps,
            int i,
            UUID parameter,
            JdbcType jdbcType
    ) throws SQLException {
        ps.setObject(i, parameter);
    }

    @Override
    public UUID getNullableResult(ResultSet rs, String columnName)
            throws SQLException {
        return rs.getObject(columnName, UUID.class);
    }

    @Override
    public UUID getNullableResult(ResultSet rs, int columnIndex)
            throws SQLException {
        return rs.getObject(columnIndex, UUID.class);
    }

    @Override
    public UUID getNullableResult(
            CallableStatement cs,
            int columnIndex
    ) throws SQLException {
        return cs.getObject(columnIndex, UUID.class);
    }
}