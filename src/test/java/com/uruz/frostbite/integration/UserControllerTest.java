package com.uruz.frostbite.integration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.uruz.frostbite.domain.Folder;
import com.uruz.frostbite.domain.FolderAcl;
import com.uruz.frostbite.domain.User;
import com.uruz.frostbite.dto.user.UserJoinRequest;
import com.uruz.frostbite.dto.user.UserJoinResponse;
import com.uruz.frostbite.dto.wrapper.ApiSucceedResponse;
import com.uruz.frostbite.enums.AclEffectType;
import com.uruz.frostbite.enums.FolderType;
import com.uruz.frostbite.enums.PermissionBitMask;
import com.uruz.frostbite.enums.user.AccountType;
import com.uruz.frostbite.enums.user.UserStatus;
import com.uruz.frostbite.security.CustomUserDetails;
import com.uruz.frostbite.service.DirectFileHandleComponent;
import com.uruz.frostbite.support.IntegrationTestSupport;
import com.uruz.frostbite.support.TestDataFixture.TestUser;
import com.uruz.frostbite.support.UserDomainEntitySupport;
import io.micrometer.common.util.StringUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
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

    // SpringBootTest를 쓰면서, 사용하지 않을 Bean까지 올라가면서 미설정으로 인해 터지는 BeanCreating Error는 아래처럼
    // MockBean -> MockitoBean으로 껍질만 등록시켜서 @PostConstruct 호출같은걸 피할 수 있다.

    // 스프링 컨텍스트 전체 구성을 할것이라면, ActiveProfiles + SpringbootTest(Properties=) 를 통해 구성정보를 줘야한다.
    // 내지는 빈 자체를 ConditionalOnProperty로 profile별로 띄워버리던가. 그러려면 의존성체인이 다 무력화되어야하겠지만.
    @MockitoBean
    private DirectFileHandleComponent directFileHandleComponent;


    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("사용자 생성")
    void joinCreatesPrivateFolderAndReturnsCreatedUser() throws Exception{
        UserJoinRequest request = UserDomainEntitySupport.joinRequest();

        MvcResult result = mockMvc.perform(post("/v1/api/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("SUCCEED"))
                .andExpect(jsonPath("$.result.name").value(request.getName()))
                .andExpect(jsonPath("$.result.email").value(request.getEmail()))
                .andExpect(jsonPath("$.result.phoneNumber").value(request.getPhoneNumber()))
                .andExpect(jsonPath("$.result.uuid").isNotEmpty())
                .andReturn();

        MockHttpServletResponse response = result.getResponse();
        String body = response.getContentAsString();

        // 내지는 이것처럼 json node 접근이 가능하다.
        /*
            JsonNode root = objectMapper.readTree(body);
            UUID userUuid = UUID.fromString(
                    root.path("result").path("uuid").asText()
            );
        * */
        JsonNode resultNode = objectMapper.readTree(body).path("result");
        UserJoinResponse joinResponse =
                objectMapper.treeToValue(resultNode, UserJoinResponse.class);

        // 사용자 정상 insert 확인
        // 결과 없으면 exception 발생

        User user = assertDoesNotThrow(() -> jdbcTemplate.queryForObject(
                """
                SELECT uuid, user_id
                FROM user_tb
                WHERE uuid = ?
                """,
                (rs, rowNum) -> {
                    User temporalUserObject = new User();
                    temporalUserObject.setUserId(rs.getInt("user_id"));
                    temporalUserObject.setUuid(rs.getObject("uuid", UUID.class));
                    return temporalUserObject;
                },
                joinResponse.getUuid()
        ), "회원가입 성공 response에도 불구하고 user_tb에서 사용자 조회 실패");

        assertThat(user).isNotNull();

        // 사용자 개인 Entry Folder insert 확인
        // 사용자 생성에 엮인 폴더 생성이 정상인지 확인되어야 하므로, UserController 측에서 직접 검증
        // 메서드의 비정상과 테스트코드의 비정상을 분리하기 위해 jdbc로 직접접근.
        // 상기 방식은 row를 직접 매핑하는 방법 (별도 변환이 필요할때) , 하기 방법은 Spring에게 기대서 자동매핑
        // 그러나, 이경우 TypeHandler를 사용하지 않는다. jdbcTemplate로 직접 들고왔기 때문..
        // 따라서 ENUM을 사용하면 -> enum의 index값으로 parsing을 시도하려하므로, 값이 1이면 -> enum의 2번째값으로 매핑되는 문제가 발생함..
        // 따라서 ENUM을 쓰는경우 직접매핑을 사용하는것이 훨씬 더 바람직함.
        /**
        BeanPropertyRowMapper<Folder> folderRowMapper = BeanPropertyRowMapper.newInstance(Folder.class);
        Folder privateEntryFolder = jdbcTemplate.queryForObject(
                """
                        SELECT uuid, folder_id, parent_folder_id,
                        resource_scope_user_id, resource_owner_id, folder_type
                        FROM folder_tb
                        WHERE resource_owner_id = ?
                        AND parent_folder_id IS null
                        """,  folderRowMapper, user.getUserId()
        );
         */
        Folder privateEntryFolder = jdbcTemplate.queryForObject(
                """
                SELECT uuid,
                       folder_id,
                       parent_folder_id,
                       resource_scope_user_id,
                       resource_owner_id,
                       folder_type
                FROM folder_tb
                WHERE resource_owner_id = ?
                  AND parent_folder_id IS NULL
                """,
                (rs, rowNum) -> {
                    Folder folder = new Folder();

                    folder.setUuid(rs.getObject("uuid", UUID.class));
                    folder.setFolderId(rs.getLong("folder_id"));

                    Long parentFolderId =
                            rs.getObject("parent_folder_id", Long.class);
                    folder.setParentFolderId(parentFolderId);

                    folder.setResourceScopeUserId(
                            rs.getObject("resource_scope_user_id", Integer.class)
                    );
                    folder.setResourceOwnerId(
                            rs.getObject("resource_owner_id", Integer.class)
                    );

                    folder.setFolderType(
                            FolderType.fromValue(rs.getInt("folder_type"))
                    );

                    return folder;
                },user.getUserId());

                // 이거 다! 통과해야 통과임! 이라는 메서드는
        // junit의 assertAll(() -> , () -> , )
        // 내지는,
        // assertJ의 assertThat(object).extracting(cols...).containsExactly(cols...); 가 있음
        // 사용자 개인폴더 ACL Default 셋팅 확인

        // 좋아 당장 둘다해보자
        // private entryFolder는 resourceScopeUserId = 생성자, resource_owner_id = 생성자
        // 하지만 적어도 resource_owner_id 는 where절에서 검증됨.
        // parentFolder는 null이어야 함

        assertAll(
                // 메시지 지정방법
                () -> assertThat(privateEntryFolder.getParentFolderId())
                        .as("Entry folder의 부모폴더는 null이어야 합니다.").isNull(),
                () -> assertThat(privateEntryFolder.getResourceScopeUserId()).isEqualTo(user.getUserId()),
                () -> assertThat(privateEntryFolder.getFolderType()).isEqualTo(FolderType.PRIVATE)
                );

        // 사용자 개인 Entry Folder에 대한 ACL 적용 여부
        // 최소한 target folder - user 에 대한 acl이 연결되어있는지 확인되어야 하고,
        // 개인폴더 소유이므로 permission bit가 모두 부여되었는지까지 검증이 되어야한다.
        // 적어도 subject_user_id 와 folder_id는 where절로 검증
        /*
        FolderAcl entryFolderInitialAcl = jdbcTemplate.queryForObject(
                """
                        SELECT acl_id, folder_id, subject_type, effect_type, subject_user_id, permission_bit_mask
                        FROM folder_acl_tb
                        WHERE folder_id = ? AND subject_user_id = ?
                        """,
                BeanPropertyRowMapper.newInstance(FolderAcl.class), privateEntryFolder.getFolderId(), user.getUserId()
        );

         */
        // ENUM을 쓰는게 문제라면 차라리 그냥 바로 MAP으로 받고 MAP에서 비교하는거도 방법이다..

        Map<String, Object> entryFolderInitialAclMap = jdbcTemplate.queryForMap(
                """
                        SELECT acl_id, folder_id, subject_type, effect_type, subject_user_id, permission_bit_mask
                        FROM folder_acl_tb
                        WHERE folder_id = ? AND subject_user_id = ?
                        """, privateEntryFolder.getFolderId(), user.getUserId());
        // 기본메시지 버리고 전체메시지로
        assertThat(entryFolderInitialAclMap).extracting(
                map -> map.get("effect_type"),
                map -> map.get("permission_bit_mask")
        ).withFailMessage(
                "ACL 설정에 문제가 있음. expectedBitMask=%s, actualBitMask=%s / expected_effect_type_value =%s, actual_effect_type_value = %s",
                PermissionBitMask.allMasks(),
                entryFolderInitialAclMap.get("permission_bit_mask"),
                AclEffectType.ALLOW.getValue(),
                entryFolderInitialAclMap.get("effect_type")
        ).containsExactly(
                AclEffectType.ALLOW.getValue(),
                PermissionBitMask.of(PermissionBitMask.allMasks())
        );

        /**
         위 사용자 테스트에 대한 고찰
            - 굳이 사용자 생성 로직에서 폴더의 권한이 잘들어갔는지, 즉, "폴더 생셩규칙" 까지 사용자 가입에서 테스트 할 필요가 있을까?
              명백한 필요사유가 있다면 해도 되지만, 이경우 사용자 테스트와 폴더 생성 테스트를 같이하는 느낌일수도.
            - 즉, "사용자 생성 시 폴더 및 ACL이 생성 안되면" 오류 지만 "ACL permissionBit가 ALLOWALL 이어야한다" 까지는
              폴더 생성 규칙이니, 폴더생성에서 테스트해도 되지 않을까?
         * */
    }

    @Test
    @DisplayName("회원가입 API는 생성된 사용자 정보를 반환한다")
    void joinReturnsCreatedUser() throws Exception {
        // given
        UserJoinRequest request = UserDomainEntitySupport.joinRequest();

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
