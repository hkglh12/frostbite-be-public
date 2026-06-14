package com.uruz.frostbite.typehandler.file;

import com.uruz.frostbite.enums.FileStatus;
import com.uruz.frostbite.enums.FolderStatus;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@MappedTypes(FileStatus.class)
@MappedJdbcTypes(value = JdbcType.SMALLINT)
public class FileStatusHandler extends BaseTypeHandler<FileStatus> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, FileStatus parameter, JdbcType jdbcType) throws SQLException {
        ps.setInt(i, parameter.getValue());
    }

    @Override
    public FileStatus getNullableResult(ResultSet rs, String columnName) throws SQLException {
        int value = rs.getInt(columnName);
        return FileStatus.fromValue(value);
    }

    @Override
    public FileStatus getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        int value = rs.getInt(columnIndex);
        return FileStatus.fromValue(value);
    }

    @Override
    public FileStatus getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        int value = cs.getInt(columnIndex);
        return FileStatus.fromValue(value);
    }
}
