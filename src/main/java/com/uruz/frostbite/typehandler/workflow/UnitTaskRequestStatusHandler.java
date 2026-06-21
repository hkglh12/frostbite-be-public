package com.uruz.frostbite.typehandler.workflow;

import com.uruz.frostbite.enums.workflow.UnitTaskRequestStatus;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@MappedTypes(UnitTaskRequestStatus.class)
public class UnitTaskRequestStatusHandler
        extends BaseTypeHandler<UnitTaskRequestStatus> {

    @Override
    public void setNonNullParameter(
            PreparedStatement ps,
            int i,
            UnitTaskRequestStatus parameter,
            JdbcType jdbcType
    ) throws SQLException {
        ps.setString(i, parameter.name());
    }

    @Override
    public UnitTaskRequestStatus getNullableResult(
            ResultSet rs,
            String columnName
    ) throws SQLException {

        String value = rs.getString(columnName);

        return value == null
                ? null
                : UnitTaskRequestStatus.valueOf(value);
    }

    @Override
    public UnitTaskRequestStatus getNullableResult(
            ResultSet rs,
            int columnIndex
    ) throws SQLException {

        String value = rs.getString(columnIndex);

        return value == null
                ? null
                : UnitTaskRequestStatus.valueOf(value);
    }

    @Override
    public UnitTaskRequestStatus getNullableResult(
            CallableStatement cs,
            int columnIndex
    ) throws SQLException {

        String value = cs.getString(columnIndex);

        return value == null
                ? null
                : UnitTaskRequestStatus.valueOf(value);
    }
}