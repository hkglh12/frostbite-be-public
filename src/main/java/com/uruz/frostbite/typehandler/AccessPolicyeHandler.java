package com.uruz.frostbite.typehandler;

import com.uruz.frostbite.enums.AccessPolicy;
import com.uruz.frostbite.enums.AclEffectType;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@MappedTypes(AccessPolicy.class)
@MappedJdbcTypes(value = JdbcType.SMALLINT)
public class AccessPolicyeHandler extends BaseTypeHandler<AccessPolicy> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, AccessPolicy parameter, JdbcType jdbcType) throws SQLException {
        ps.setInt(i, parameter.getValue());
    }

    @Override
    public AccessPolicy getNullableResult(ResultSet rs, String columnName) throws SQLException {
        int value = rs.getInt(columnName);
        return AccessPolicy.fromValue(value);
    }

    @Override
    public AccessPolicy getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        int value = rs.getInt(columnIndex);
        return AccessPolicy.fromValue(value);
    }

    @Override
    public AccessPolicy getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        int value = cs.getInt(columnIndex);
        return AccessPolicy.fromValue(value);
    }
}
