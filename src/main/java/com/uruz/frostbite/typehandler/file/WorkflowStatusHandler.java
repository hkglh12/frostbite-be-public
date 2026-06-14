package com.uruz.frostbite.typehandler.file;

import com.uruz.frostbite.enums.FolderStatus;
import com.uruz.frostbite.enums.WorkflowStatus;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@MappedTypes(WorkflowStatus.class)
@MappedJdbcTypes(value = JdbcType.SMALLINT)
public class WorkflowStatusHandler extends BaseTypeHandler<WorkflowStatus> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, com.uruz.frostbite.enums.WorkflowStatus parameter, JdbcType jdbcType) throws SQLException {
        ps.setInt(i, parameter.getValue());
    }

    @Override
    public WorkflowStatus getNullableResult(ResultSet rs, String columnName) throws SQLException {
        int value = rs.getInt(columnName);
        return WorkflowStatus.fromValue(value);
    }

    @Override
    public WorkflowStatus getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        int value = rs.getInt(columnIndex);
        return WorkflowStatus.fromValue(value);
    }

    @Override
    public WorkflowStatus getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        int value = cs.getInt(columnIndex);
        return WorkflowStatus.fromValue(value);
    }
}
