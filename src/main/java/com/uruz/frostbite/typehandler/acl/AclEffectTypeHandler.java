package com.uruz.frostbite.typehandler.acl;

import com.uruz.frostbite.enums.AclEffectType;
import com.uruz.frostbite.enums.AclSubjectType;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@MappedTypes(AclEffectType.class)
@MappedJdbcTypes(value = JdbcType.SMALLINT)
public class AclEffectTypeHandler extends BaseTypeHandler<AclEffectType> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, AclEffectType parameter, JdbcType jdbcType) throws SQLException {
        ps.setInt(i, parameter.getValue());
    }

    @Override
    public AclEffectType getNullableResult(ResultSet rs, String columnName) throws SQLException {
        int value = rs.getInt(columnName);
        return AclEffectType.fromValue(value);
    }

    @Override
    public AclEffectType getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        int value = rs.getInt(columnIndex);
        return AclEffectType.fromValue(value);
    }

    @Override
    public AclEffectType getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        int value = cs.getInt(columnIndex);
        return AclEffectType.fromValue(value);
    }
}
