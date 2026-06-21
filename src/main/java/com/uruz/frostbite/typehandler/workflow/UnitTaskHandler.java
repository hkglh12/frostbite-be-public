package com.uruz.frostbite.typehandler.workflow;

import com.uruz.frostbite.enums.workflow.UnitTask;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@MappedTypes(UnitTask.class)
public class UnitTaskHandler
        extends BaseTypeHandler<UnitTask> {

    @Override
    public void setNonNullParameter(
            PreparedStatement ps,
            int i,
            UnitTask parameter,
            JdbcType jdbcType
    ) throws SQLException {
        ps.setString(i, parameter.name());
    }

    @Override
    public UnitTask getNullableResult(
            ResultSet rs,
            String columnName
    ) throws SQLException {

        String value = rs.getString(columnName);

        return value == null
                ? null
                : UnitTask.valueOf(value);
    }

    @Override
    public UnitTask getNullableResult(
            ResultSet rs,
            int columnIndex
    ) throws SQLException {

        String value = rs.getString(columnIndex);

        return value == null
                ? null
                : UnitTask.valueOf(value);
    }

    @Override
    public UnitTask getNullableResult(
            CallableStatement cs,
            int columnIndex
    ) throws SQLException {

        String value = cs.getString(columnIndex);

        return value == null
                ? null
                : UnitTask.valueOf(value);
    }
}