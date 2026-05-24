package com.uruz.frostbite.typehandler.acl;

import com.uruz.frostbite.enums.AclStatus;
import com.uruz.frostbite.enums.AclSubjectType;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@MappedTypes(AclStatus.class)
@MappedJdbcTypes(value = JdbcType.SMALLINT)
public class AclStatusHandler extends BaseTypeHandler<AclStatus> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, AclStatus parameter, JdbcType jdbcType) throws SQLException {
        ps.setInt(i, parameter.getValue());
    }

    @Override
    public AclStatus getNullableResult(ResultSet rs, String columnName) throws SQLException {
        int value = rs.getInt(columnName);
        return AclStatus.fromValue(value);
    }

    @Override
    public AclStatus getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        int value = rs.getInt(columnIndex);
        return AclStatus.fromValue(value);
    }

    @Override
    public AclStatus getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        int value = cs.getInt(columnIndex);
        return AclStatus.fromValue(value);
    }
}
