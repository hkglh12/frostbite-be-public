package com.uruz.frostbite.typehandler.user;

import com.uruz.frostbite.enums.user.AccountType;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@MappedTypes(AccountType.class)
@MappedJdbcTypes(value = JdbcType.SMALLINT)
public class AccountTypeHandler extends BaseTypeHandler<AccountType> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, AccountType parameter, JdbcType jdbcType) throws SQLException {
        ps.setInt(i, parameter.getValue());
    }

    @Override
    public AccountType getNullableResult(ResultSet rs, String columnName) throws SQLException {
        int value = rs.getInt(columnName);
        return AccountType.fromValue(value);
    }

    @Override
    public AccountType getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        int value = rs.getInt(columnIndex);
        return AccountType.fromValue(value);
    }

    @Override
    public AccountType getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        int value = cs.getInt(columnIndex);
        return AccountType.fromValue(value);
    }
}
