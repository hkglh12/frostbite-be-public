package com.uruz.frostbite.typehandler.workflow;

import com.uruz.frostbite.enums.workflow.UnitTaskRequestStatus;
import com.uruz.frostbite.enums.workflow.WorkflowRequestStatus;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@MappedTypes(WorkflowRequestStatus.class)
public class WorkflowRequestStatusHandler
        extends BaseTypeHandler<WorkflowRequestStatus> {

    @Override
    public void setNonNullParameter(
            PreparedStatement ps,
            int i,
            WorkflowRequestStatus parameter,
            JdbcType jdbcType
    ) throws SQLException {
        ps.setString(i, parameter.name());
    }

    @Override
    public WorkflowRequestStatus getNullableResult(
            ResultSet rs,
            String columnName
    ) throws SQLException {

        String value = rs.getString(columnName);

        return value == null
                ? null
                : WorkflowRequestStatus.valueOf(value);
    }

    @Override
    public WorkflowRequestStatus getNullableResult(
            ResultSet rs,
            int columnIndex
    ) throws SQLException {

        String value = rs.getString(columnIndex);

        return value == null
                ? null
                : WorkflowRequestStatus.valueOf(value);
    }

    @Override
    public WorkflowRequestStatus getNullableResult(
            CallableStatement cs,
            int columnIndex
    ) throws SQLException {

        String value = cs.getString(columnIndex);

        return value == null
                ? null
                : WorkflowRequestStatus.valueOf(value);
    }
}