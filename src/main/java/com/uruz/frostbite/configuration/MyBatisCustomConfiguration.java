package com.uruz.frostbite.configuration;

import org.apache.ibatis.type.JdbcType;
import org.mybatis.spring.boot.autoconfigure.ConfigurationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MyBatisCustomConfiguration {
    @Bean
    public ConfigurationCustomizer mybatisConfiguration(){
        return configuration -> {
            configuration.setMapUnderscoreToCamelCase(true);
            configuration.setJdbcTypeForNull(JdbcType.NULL);
            configuration.setUseGeneratedKeys(true);
        };
    }
}
