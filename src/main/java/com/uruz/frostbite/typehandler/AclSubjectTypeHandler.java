package com.uruz.frostbite.typehandler;

import com.uruz.frostbite.enums.AclSubjectType;
import com.uruz.frostbite.enums.PermissionBitMask;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@MappedTypes(AclSubjectType.class)
@MappedJdbcTypes(value = JdbcType.SMALLINT)
public class AclSubjectTypeHandler extends BaseTypeHandler<AclSubjectType> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, AclSubjectType parameter, JdbcType jdbcType) throws SQLException {
        ps.setInt(i, parameter.getValue());
    }

    @Override
    public AclSubjectType getNullableResult(ResultSet rs, String columnName) throws SQLException {
        int value = rs.getInt(columnName);
        return AclSubjectType.fromValue(value);
    }

    @Override
    public AclSubjectType getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        int value = rs.getInt(columnIndex);
        return AclSubjectType.fromValue(value);
    }

    @Override
    public AclSubjectType getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        int value = cs.getInt(columnIndex);
        return AclSubjectType.fromValue(value);
    }
}
