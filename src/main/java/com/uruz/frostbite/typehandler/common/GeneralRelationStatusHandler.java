package com.uruz.frostbite.typehandler.common;

import com.uruz.frostbite.enums.common.GeneralRelationStatus;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@MappedTypes(GeneralRelationStatus.class)
@MappedJdbcTypes(value = JdbcType.SMALLINT)
public class GeneralRelationStatusHandler extends BaseTypeHandler<GeneralRelationStatus> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, GeneralRelationStatus parameter, JdbcType jdbcType) throws SQLException {
        ps.setInt(i, parameter.getValue());
    }

    @Override
    public GeneralRelationStatus getNullableResult(ResultSet rs, String columnName) throws SQLException {
        int value = rs.getInt(columnName);
        return GeneralRelationStatus.fromValue(value);
    }

    @Override
    public GeneralRelationStatus getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        int value = rs.getInt(columnIndex);
        return GeneralRelationStatus.fromValue(value);
    }

    @Override
    public GeneralRelationStatus getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        int value = cs.getInt(columnIndex);
        return GeneralRelationStatus.fromValue(value);
    }
}
