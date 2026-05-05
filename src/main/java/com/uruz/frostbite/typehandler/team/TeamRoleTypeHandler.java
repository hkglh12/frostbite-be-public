package com.uruz.frostbite.typehandler.team;

import com.uruz.frostbite.enums.team.TeamRole;
import com.uruz.frostbite.enums.team.TeamStatus;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@MappedTypes(TeamRole.class)
@MappedJdbcTypes(value = JdbcType.SMALLINT)
public class TeamRoleTypeHandler extends BaseTypeHandler<TeamRole> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, TeamRole parameter, JdbcType jdbcType) throws SQLException {
        ps.setInt(i, parameter.getValue());
    }

    @Override
    public TeamRole getNullableResult(ResultSet rs, String columnName) throws SQLException {
        int value = rs.getInt(columnName);
        return TeamRole.fromValue(value);
    }

    @Override
    public TeamRole getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        int value = rs.getInt(columnIndex);
        return TeamRole.fromValue(value);
    }

    @Override
    public TeamRole getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        int value = cs.getInt(columnIndex);
        return TeamRole.fromValue(value);
    }
}
