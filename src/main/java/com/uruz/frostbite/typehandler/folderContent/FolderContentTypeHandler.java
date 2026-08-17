package com.uruz.frostbite.typehandler.folderContent;

import com.uruz.frostbite.enums.entry.FolderContentType;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@MappedTypes(FolderContentType.class)
@MappedJdbcTypes(value = JdbcType.SMALLINT)
public class FolderContentTypeHandler extends BaseTypeHandler<FolderContentType> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, FolderContentType parameter, JdbcType jdbcType) throws SQLException {
        ps.setInt(i, parameter.getValue());
    }

    @Override
    public FolderContentType getNullableResult(ResultSet rs, String columnName) throws SQLException {
        int value = rs.getInt(columnName);
        return FolderContentType.fromValue(value);
    }

    @Override
    public FolderContentType getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        int value = rs.getInt(columnIndex);
        return FolderContentType.fromValue(value);
    }

    @Override
    public FolderContentType getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        int value = cs.getInt(columnIndex);
        return FolderContentType.fromValue(value);
    }
}
