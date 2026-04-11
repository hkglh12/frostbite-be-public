package com.uruz.frostbite.configuration;

import com.uruz.frostbite.typehandler.UUIDTypeHandler;
import org.mybatis.spring.boot.autoconfigure.ConfigurationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MyBatisCustomConfiguration {
    @Bean
    public ConfigurationCustomizer mybatisConfiguration(){
        return configuration -> {
            configuration.setMapUnderscoreToCamelCase(true);
            configuration.setJdbcTypeForNull(null);
            configuration.setUseGeneratedKeys(true);
        };
    }
}
