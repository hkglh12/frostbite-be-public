package com.uruz.frostbite.typehandler.file;

import com.uruz.frostbite.enums.FileStatus;
import com.uruz.frostbite.enums.FileTempStatus;
import com.uruz.frostbite.enums.FolderStatus;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@MappedTypes(FileTempStatus.class)
@MappedJdbcTypes(value = JdbcType.SMALLINT)
public class FileTempStatusHandler extends BaseTypeHandler<FileTempStatus> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, FileTempStatus parameter, JdbcType jdbcType) throws SQLException {
        ps.setInt(i, parameter.getValue());
    }

    @Override
    public FileTempStatus getNullableResult(ResultSet rs, String columnName) throws SQLException {
        int value = rs.getInt(columnName);
        return FileTempStatus.fromValue(value);
    }

    @Override
    public FileTempStatus getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        int value = rs.getInt(columnIndex);
        return FileTempStatus.fromValue(value);
    }

    @Override
    public FileTempStatus getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        int value = cs.getInt(columnIndex);
        return FileTempStatus.fromValue(value);
    }
}
