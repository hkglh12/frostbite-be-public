package com.uruz.frostbite.typehandler;

import com.uruz.frostbite.enums.FolderType;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@MappedTypes(FolderType.class)
@MappedJdbcTypes(value = JdbcType.SMALLINT)
public class FolderTypeHandler extends BaseTypeHandler<FolderType> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, FolderType parameter, JdbcType jdbcType) throws SQLException {
        ps.setInt(i, parameter.getValue());
    }

    @Override
    public FolderType getNullableResult(ResultSet rs, String columnName) throws SQLException {
        int value = rs.getInt(columnName);
        return FolderType.fromValue(value);
    }

    @Override
    public FolderType getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        int value = rs.getInt(columnIndex);
        return FolderType.fromValue(value);
    }

    @Override
    public FolderType getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        int value = cs.getInt(columnIndex);
        return FolderType.fromValue(value);
    }
}
