package com.uruz.frostbite.typehandler.workflow;

import com.uruz.frostbite.enums.workflow.Workflow;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@MappedTypes(Workflow.class)
public class WorkflowHandler
        extends BaseTypeHandler<Workflow> {

    @Override
    public void setNonNullParameter(
            PreparedStatement ps,
            int i,
            Workflow parameter,
            JdbcType jdbcType
    ) throws SQLException {
        ps.setString(i, parameter.name());
    }

    @Override
    public Workflow getNullableResult(
            ResultSet rs,
            String columnName
    ) throws SQLException {

        String value = rs.getString(columnName);

        return value == null
                ? null
                : Workflow.valueOf(value);
    }

    @Override
    public Workflow getNullableResult(
            ResultSet rs,
            int columnIndex
    ) throws SQLException {

        String value = rs.getString(columnIndex);

        return value == null
                ? null
                : Workflow.valueOf(value);
    }

    @Override
    public Workflow getNullableResult(
            CallableStatement cs,
            int columnIndex
    ) throws SQLException {

        String value = cs.getString(columnIndex);

        return value == null
                ? null
                : Workflow.valueOf(value);
    }
}