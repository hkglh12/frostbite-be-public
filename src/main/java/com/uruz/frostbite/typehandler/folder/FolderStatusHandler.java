package com.uruz.frostbite.typehandler.folder;

import com.uruz.frostbite.enums.FolderStatus;
import com.uruz.frostbite.enums.FolderType;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@MappedTypes(FolderStatus.class)
@MappedJdbcTypes(value = JdbcType.SMALLINT)
public class FolderStatusHandler extends BaseTypeHandler<FolderStatus> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, FolderStatus parameter, JdbcType jdbcType) throws SQLException {
        ps.setInt(i, parameter.getValue());
    }

    @Override
    public FolderStatus getNullableResult(ResultSet rs, String columnName) throws SQLException {
        int value = rs.getInt(columnName);
        return FolderStatus.fromValue(value);
    }

    @Override
    public FolderStatus getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        int value = rs.getInt(columnIndex);
        return FolderStatus.fromValue(value);
    }

    @Override
    public FolderStatus getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        int value = cs.getInt(columnIndex);
        return FolderStatus.fromValue(value);
    }
}
