package com.uruz.frostbite.typehandler.workflow;

import com.uruz.frostbite.enums.workflow.WorkflowDomainType;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@MappedTypes(WorkflowDomainType.class)
public class WorkflowTargetTypeHandler
        extends BaseTypeHandler<WorkflowDomainType> {

    @Override
    public void setNonNullParameter(
            PreparedStatement ps,
            int i,
            WorkflowDomainType parameter,
            JdbcType jdbcType
    ) throws SQLException {
        ps.setString(i, parameter.name());
    }

    @Override
    public WorkflowDomainType getNullableResult(
            ResultSet rs,
            String columnName
    ) throws SQLException {

        String value = rs.getString(columnName);

        return value == null
                ? null
                : WorkflowDomainType.valueOf(value);
    }

    @Override
    public WorkflowDomainType getNullableResult(
            ResultSet rs,
            int columnIndex
    ) throws SQLException {

        String value = rs.getString(columnIndex);

        return value == null
                ? null
                : WorkflowDomainType.valueOf(value);
    }

    @Override
    public WorkflowDomainType getNullableResult(
            CallableStatement cs,
            int columnIndex
    ) throws SQLException {

        String value = cs.getString(columnIndex);

        return value == null
                ? null
                : WorkflowDomainType.valueOf(value);
    }
}