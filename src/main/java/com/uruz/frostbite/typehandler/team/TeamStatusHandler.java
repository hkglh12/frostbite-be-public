package com.uruz.frostbite.typehandler.team;

import com.uruz.frostbite.enums.team.TeamStatus;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@MappedTypes(TeamStatus.class)
@MappedJdbcTypes(value = JdbcType.SMALLINT)
public class TeamStatusHandler extends BaseTypeHandler<TeamStatus> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, TeamStatus parameter, JdbcType jdbcType) throws SQLException {
        ps.setInt(i, parameter.getValue());
    }

    @Override
    public TeamStatus getNullableResult(ResultSet rs, String columnName) throws SQLException {
        int value = rs.getInt(columnName);
        return TeamStatus.fromValue(value);
    }

    @Override
    public TeamStatus getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        int value = rs.getInt(columnIndex);
        return TeamStatus.fromValue(value);
    }

    @Override
    public TeamStatus getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        int value = cs.getInt(columnIndex);
        return TeamStatus.fromValue(value);
    }
}
