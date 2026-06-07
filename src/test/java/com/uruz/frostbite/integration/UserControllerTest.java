package com.uruz.frostbite.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uruz.frostbite.domain.User;
import com.uruz.frostbite.dto.user.UserJoinRequest;
import com.uruz.frostbite.enums.user.AccountType;
import com.uruz.frostbite.enums.user.UserStatus;
import com.uruz.frostbite.security.CustomUserDetails;
import com.uruz.frostbite.support.IntegrationTestSupport;
import com.uruz.frostbite.support.TestDataFixture.TestUser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@DisplayName("UserController 통합 테스트")
class UserControllerTest extends IntegrationTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("회원가입 API는 생성된 사용자 정보를 반환한다")
    void joinReturnsCreatedUser() throws Exception {
        // given
        UserJoinRequest request = UserServiceTest.joinRequest();

        // when & then
        mockMvc.perform(post("/v1/api/user/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("SUCCEED"))
                .andExpect(jsonPath("$.result.name").value(request.getName()))
                .andExpect(jsonPath("$.result.email").value(request.getEmail()))
                .andExpect(jsonPath("$.result.phoneNumber").value(request.getPhoneNumber()))
                .andExpect(jsonPath("$.result.uuid").isNotEmpty());
    }

    @Test
    @DisplayName("본인 조회 API는 인증 주체의 userId로 사용자를 조회한다")
    void getSelfReturnsAuthenticatedUser() throws Exception {
        // given
        TestUser testUser = fixture.createUser();
        CustomUserDetails principal = principal(testUser);

        // when & then
        mockMvc.perform(get("/v1/api/user/").with(user(principal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("SUCCEED"))
                .andExpect(jsonPath("$.result.name").value(testUser.name()))
                .andExpect(jsonPath("$.result.email").value(testUser.email()))
                .andExpect(jsonPath("$.result.uuid").value(testUser.uuid().toString()));
    }

    private static CustomUserDetails principal(TestUser testUser) {
        User user = new User();
        user.setUserId(testUser.id());
        user.setUuid(testUser.uuid());
        user.setName(testUser.name());
        user.setEmail(testUser.email());
        user.setPassword("encoded-password");
        user.setAccountType(AccountType.USER);
        user.setStatus(UserStatus.ACTIVE);
        return CustomUserDetails.fromUser(user);
    }
}
