package com.uruz.frostbite.testSupports;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uruz.frostbite.dto.user.UserPasswordModifyRequest;
import com.uruz.frostbite.support.IntegrationTestSupport;
import com.uruz.frostbite.support.TestDataFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@DisplayName("UserService 테스트 지원용 강제 셋업")
@SpringBootTest(properties = {
        "spring.config.import=file:C:/Users/hkglh/Projects/sideProject/frostbite/application-secret.properties"
})
public class UserServiceSupports extends IntegrationTestSupport {

    @Autowired
    private BCryptPasswordEncoder encoder;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void modifyUserSelfPassword() throws Exception{

        int affected = jdbcTemplate.update(
                """
                        
                        update user_tb set password = ? where email = ?
                        """,  encoder.encode("coicxa#1107!"),"hkglh12@gmail.com"
        );
    }
}
