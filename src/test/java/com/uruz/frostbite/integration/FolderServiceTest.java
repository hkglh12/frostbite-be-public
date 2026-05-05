package com.uruz.frostbite.integration;

import com.uruz.frostbite.customException.GeneralCustomException;
import com.uruz.frostbite.domain.Folder;
import com.uruz.frostbite.dto.folder.FolderCreateRequest;
import com.uruz.frostbite.dto.folder.SubFolderMoveRequest;
import com.uruz.frostbite.dto.folder.TeamRootFolderCreateRequest;
import com.uruz.frostbite.enums.FolderStatus;
import com.uruz.frostbite.enums.FolderType;
import com.uruz.frostbite.service.FolderService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * FolderService 서비스 레이어 통합 테스트
 *
 * [전제 조건]
 * - PostgreSQL (localhost:9413) 및 Docker 컨테이너가 실행 중이어야 한다.
 * - 아래 TODO 상수에 실제 테스트 대상 사용자의 UUID와 폴더명을 채워 넣어야 한다.
 * - 해당 UUID로 folder_tb에 PRIVATE 엔트리폴더(parent_id IS NULL)가 이미 존재해야 한다.
 *
 * [현재 알려진 WIP 이슈 - 테스트 실행 전 확인 필요]
 * 1. FolderService.createPrivateRootFolder() 가 newFolder 대신 null 을 리턴한다.
 *    → 리턴값 기반 단언은 해당 버그 수정 후 활성화할 것 (주석 처리된 assert 참고)
 * 2. FolderMapper.xml 의 select id 가 "getPrivateEntryFolder" 이고
 *    FolderMapper.java 인터페이스 메서드명은 "selectPrivateEntryFolder" 로 불일치한다.
 *    → 테스트 실행 시 MyBatis가 Statement를 찾지 못해 예외가 발생할 수 있다.
 * 3. FolderMapper.xml 에 insertRootFolder 가 중복 정의되어 있다.
 *
 * [트랜잭션 전략]
 * @Transactional 선언으로 각 테스트 메서드 종료 후 자동 롤백.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@TestPropertySource(locations = "classpath:application-test.properties")
@DisplayName("FolderService 통합 테스트")
class FolderServiceTest {

    @Autowired
    private FolderService folderService;

    // ── TODO: [createPrivateRootFolder] 파라미터를 직접 채워 넣으세요 ─────────
    private static final UUID   TEST_USER_UUID  = UUID.fromString("cb548410-c81f-4c82-8ebb-299eaeff8362");
    private static final String TEST_FOLDER_NAME = "폴더 테스트";
    // ─────────────────────────────────────────────────────────────────────────

    // ── TODO: [createTeamRootFolder] 파라미터를 직접 채워 넣으세요 ────────────
    // 전제: team_tb 에 해당 UUID 의 팀이 존재하고,
    //       folder_tb 에 해당 팀의 TEAM 엔트리폴더(parent_id IS NULL)가 있어야 한다.
    private static final UUID   TEST_TEAM_USER_UUID  = UUID.fromString("00000000-0000-0000-0000-000000000000"); // 요청 유저 UUID
    private static final UUID   TEST_TEAM_UUID        = UUID.fromString("00000000-0000-0000-0000-000000000000"); // 팀 UUID
    private static final String TEST_TEAM_FOLDER_NAME = "팀 폴더 테스트";
    // ─────────────────────────────────────────────────────────────────────────

    // ── TODO: [createSubFolder - PRIVATE] 파라미터를 직접 채워 넣으세요 ────────
    // 전제: folder_tb 에 해당 UUID 의 PRIVATE 폴더가 이미 존재해야 한다.
    private static final UUID   TEST_PRIVATE_SUB_USER_UUID          = UUID.fromString("cb548410-c81f-4c82-8ebb-299eaeff8362"); // 요청 유저 UUID
    private static final UUID   TEST_PRIVATE_SUB_PARENT_FOLDER_UUID = UUID.fromString("1fd80a6e-5d6d-4b4f-b97d-f2a16de39c43"); // 부모 폴더 UUID
    private static final String TEST_PRIVATE_SUB_FOLDER_NAME        = "개인 서브폴더 테스트";
    // ─────────────────────────────────────────────────────────────────────────

    // ── TODO: [성능테스트] 파라미터를 직접 채워 넣으세요 ─────────────────────────
    // 전제: folder_tb 에 해당 UUID 의 폴더가 이미 존재해야 한다.
    // 주의: 생성되는 폴더 수 ≈ 3,495,250개. 데이터 정리 후 실행할 것.
    private static final UUID       TEST_PERF_USER_UUID    = UUID.fromString("cb548410-c81f-4c82-8ebb-299eaeff8362"); // 요청 유저 UUID
    private static final UUID       TEST_PERF_ROOT_UUID    = UUID.fromString("1fd80a6e-5d6d-4b4f-b97d-f2a16de39c43"); // 트리 루트 폴더 UUID
    private static final FolderType TEST_PERF_FOLDER_TYPE  = FolderType.PRIVATE;
    // ─────────────────────────────────────────────────────────────────────────

    // ── TODO: [moveSubFolder - 정상] 파라미터를 직접 채워 넣으세요 ─────────────
    // 전제:
    //   - TEST_MOVE_USER_ID       : 요청 유저의 Integer PK (user_tb.user_id)
    //   - TEST_MOVE_TARGET_UUID   : 이동할 SubFolder UUID (parent_id IS NOT NULL, ROOT/ENTRY 아님)
    //   - TEST_MOVE_DEST_UUID     : 이동 목적지 부모폴더 UUID
    //                               (targetFolder 와 folderType·ownerId 가 동일해야 함)
    private static final Integer TEST_MOVE_USER_ID      = 7;                                              // TODO
    private static final UUID    TEST_MOVE_TARGET_UUID  = UUID.fromString("2a90c5a5-522b-422f-bf90-4fe3f60678c4"); // TODO
    private static final UUID    TEST_MOVE_DEST_UUID    = UUID.fromString("c9cd5947-6607-4e5d-8713-5af41b50cd15"); // TODO
    // ─────────────────────────────────────────────────────────────────────────

    // ── TODO: [moveSubFolder - FolderType 불일치] 파라미터를 직접 채워 넣으세요 ──
    // 전제: target 과 destination 의 folderType 이 달라야 한다 (예: PRIVATE vs TEAM)
    private static final Integer TEST_MOVE_TYPE_MISMATCH_USER_ID     = 0;                                              // TODO
    private static final UUID    TEST_MOVE_TYPE_MISMATCH_TARGET_UUID = UUID.fromString("00000000-0000-0000-0000-000000000000"); // TODO
    private static final UUID    TEST_MOVE_TYPE_MISMATCH_DEST_UUID   = UUID.fromString("00000000-0000-0000-0000-000000000000"); // TODO
    // ─────────────────────────────────────────────────────────────────────────

    // ── TODO: [moveSubFolder - Owner 불일치] 파라미터를 직접 채워 넣으세요 ────────
    // 전제: target 과 destination 의 folderType 은 같지만 ownerId 가 달라야 한다
    private static final Integer TEST_MOVE_OWNER_MISMATCH_USER_ID     = 0;                                              // TODO
    private static final UUID    TEST_MOVE_OWNER_MISMATCH_TARGET_UUID = UUID.fromString("00000000-0000-0000-0000-000000000000"); // TODO
    private static final UUID    TEST_MOVE_OWNER_MISMATCH_DEST_UUID   = UUID.fromString("00000000-0000-0000-0000-000000000000"); // TODO
    // ─────────────────────────────────────────────────────────────────────────

    // ── TODO: [createSubFolder - TEAM] 파라미터를 직접 채워 넣으세요 ────────────
    // 전제: folder_tb 에 해당 UUID 의 TEAM 폴더가 이미 존재해야 한다.
    private static final UUID   TEST_TEAM_SUB_USER_UUID          = UUID.fromString("00000000-0000-0000-0000-000000000000"); // 요청 유저 UUID
    private static final UUID   TEST_TEAM_SUB_PARENT_FOLDER_UUID = UUID.fromString("00000000-0000-0000-0000-000000000000"); // 부모 폴더 UUID
    private static final String TEST_TEAM_SUB_FOLDER_NAME        = "팀 서브폴더 테스트";
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @Transactional
    @DisplayName("[createPrivateRootFolder] 개인 엔트리폴더 하위에 루트폴더가 생성된다")
    void createPrivateRootFolder_inserts_folder_under_private_entry() {

        Folder result = folderService.createPrivateRootFolder(TEST_USER_UUID, TEST_FOLDER_NAME);

        // NOTE: 현재 서비스 메서드가 null 을 리턴하는 WIP 상태이므로
        //       아래 단언은 리턴값 버그 수정 후 주석 해제하여 사용할 것.

        assertNotNull(result,                             "생성된 폴더 객체가 반환되어야 합니다");
        assertEquals(TEST_FOLDER_NAME, result.getName(), "폴더명이 요청과 일치해야 합니다");
        assertNotNull(result.getFolderId(),              "DB sequence 로 folderId 가 채워져야 합니다");
        assertNotNull(result.getUuid(),                  "UUID 가 생성되어야 합니다");
        assertNotNull(result.getParentFolderId(),              "엔트리폴더 id 가 parentId 로 설정되어야 합니다");
        assertEquals(FolderType.PRIVATE, result.getFolderType(), "폴더 타입이 PRIVATE 이어야 합니다");
        assertEquals(FolderStatus.ACTIVE, result.getStatus(),    "초기 상태는 ACTIVE 여야 합니다");

    }

    @Test
    @Transactional
    @DisplayName("[createTeamRootFolder] 팀 엔트리폴더 하위에 루트폴더가 생성된다")
    void createTeamRootFolder_inserts_folder_under_team_entry() {

        TeamRootFolderCreateRequest request = new TeamRootFolderCreateRequest();
        request.setName(TEST_TEAM_FOLDER_NAME);
        request.setTeamUuid(TEST_TEAM_UUID);

        Folder result = folderService.createTeamRootFolder(TEST_TEAM_USER_UUID, request);

        assertNotNull(result,                                  "생성된 폴더 객체가 반환되어야 합니다");
        assertEquals(TEST_TEAM_FOLDER_NAME, result.getName(), "폴더명이 요청과 일치해야 합니다");
        assertNotNull(result.getFolderId(),                    "DB sequence 로 folderId 가 채워져야 합니다");
        assertNotNull(result.getUuid(),                        "UUID 가 생성되어야 합니다");
        assertNotNull(result.getParentFolderId(),                    "팀 엔트리폴더 id 가 parentId 로 설정되어야 합니다");
        assertEquals(FolderType.TEAM, result.getFolderType(),  "폴더 타입이 TEAM 이어야 합니다");
        assertEquals(FolderStatus.ACTIVE, result.getStatus(),  "초기 상태는 ACTIVE 여야 합니다");
    }

    @Test
    @Commit
    @DisplayName("[createSubFolder - PRIVATE] 개인 폴더 하위에 서브폴더가 생성된다")
    void createSubFolder_private_inserts_subfolder_under_private_folder() {

        FolderCreateRequest request = new FolderCreateRequest();
        request.setName(TEST_PRIVATE_SUB_FOLDER_NAME);
        request.setParentFolderUuid(TEST_PRIVATE_SUB_PARENT_FOLDER_UUID);
        request.setFolderType(FolderType.PRIVATE);

        Folder result = folderService.createFolder(TEST_PRIVATE_SUB_USER_UUID, request);


        assertNotNull(result,                                          "생성된 폴더 객체가 반환되어야 합니다");
        assertEquals(TEST_PRIVATE_SUB_FOLDER_NAME, result.getName(),  "폴더명이 요청과 일치해야 합니다");
        assertNotNull(result.getFolderId(),                            "DB sequence 로 folderId 가 채워져야 합니다");
        assertNotNull(result.getUuid(),                                "UUID 가 생성되어야 합니다");
        assertNotNull(result.getParentFolderId(),                      "부모 폴더 id 가 parentFolderId 로 설정되어야 합니다");
        assertNotNull(result.getOwnerId(),                             "userId 가 ownerId 로 설정되어야 합니다");
        assertEquals(FolderType.PRIVATE, result.getFolderType(),       "폴더 타입이 PRIVATE 이어야 합니다");
        assertEquals(FolderStatus.ACTIVE, result.getStatus(),          "초기 상태는 ACTIVE 여야 합니다");
    }

    @Test
    @Transactional
    @DisplayName("[createSubFolder - TEAM] 팀 폴더 하위에 서브폴더가 생성된다")
    void createSubFolder_team_inserts_subfolder_under_team_folder() {

        FolderCreateRequest request = new FolderCreateRequest();
        request.setName(TEST_TEAM_SUB_FOLDER_NAME);
        request.setParentFolderUuid(TEST_TEAM_SUB_PARENT_FOLDER_UUID);
        request.setFolderType(FolderType.TEAM);

        Folder result = folderService.createFolder(TEST_TEAM_SUB_USER_UUID, request);

        assertNotNull(result,                                        "생성된 폴더 객체가 반환되어야 합니다");
        assertEquals(TEST_TEAM_SUB_FOLDER_NAME, result.getName(),   "폴더명이 요청과 일치해야 합니다");
        assertNotNull(result.getFolderId(),                          "DB sequence 로 folderId 가 채워져야 합니다");
        assertNotNull(result.getUuid(),                              "UUID 가 생성되어야 합니다");
        assertNotNull(result.getParentFolderId(),                    "부모 폴더 id 가 parentFolderId 로 설정되어야 합니다");
        assertNotNull(result.getOwnerId(),                           "팀 id 가 ownerId 로 설정되어야 합니다");
        assertEquals(FolderType.TEAM, result.getFolderType(),        "폴더 타입이 TEAM 이어야 합니다");
        assertEquals(FolderStatus.ACTIVE, result.getStatus(),        "초기 상태는 ACTIVE 여야 합니다");
    }

    /**
     * [성능] 10 Depth 폴더 트리 더미 데이터 생성
     *
     * Depth 1 : ROOT 하위에 10개
     * Depth 2~10 : 직전 레벨 폴더마다 4개씩 (총 ≈ 3,495,250개)
     *
     * [메모리 전략]
     * 직전 레벨의 UUID 목록만 List<UUID>로 유지하고, 자식 생성 시 createSubFolder 내부에서
     * DB를 재조회하므로 Folder 객체를 레벨 간에 보유하지 않는다.
     * 피크 메모리: depth 9 UUID 655,360개 × ~80 bytes ≈ 50 MB
     *
     * 단언 없음 — DB 성능 확인용 더미 데이터 생성 절차.
     */
    @Test
    @DisplayName("[성능] 10 Depth 폴더 트리 더미 데이터 생성")
    void createFolderTree_10depth_dummy_data_for_performance() {
        final int DEPTH_1_COUNT  = 10;
        final int CHILD_PER_NODE = 4;
        final int MAX_DEPTH      = 6;
        final int BATCH_SIZE     = 200;  // 한 배치에서 처리할 부모 수

        List<UUID> currentLevel = new ArrayList<>(DEPTH_1_COUNT);
        long totalCreated = 0;

        // ── Depth 1: 지정 루트 폴더 하위에 10개 ──────────────────────────────
        for (int i = 1; i <= DEPTH_1_COUNT; i++) {
            Folder created = folderService.createFolder(
                    TEST_PERF_USER_UUID, buildPerfRequest("d1-" + i, TEST_PERF_ROOT_UUID));
            currentLevel.add(created.getUuid());
        }
        totalCreated += currentLevel.size();
        System.out.printf("[Depth  1] %,8d개 생성  (누적 %,11d개)%n", (long) currentLevel.size(), totalCreated);

        // ── Depth 2~10: 이전 레벨 UUID 배치 소진 → 자식 생성 ─────────────────
        for (int depth = 2; depth <= MAX_DEPTH; depth++) {
            boolean isLastDepth = (depth == MAX_DEPTH);
            List<UUID> nextLevel = new ArrayList<>();
            long depthCreated = 0;

            for (int offset = 0; offset < currentLevel.size(); offset += BATCH_SIZE) {
                List<UUID> batch = currentLevel.subList(offset, Math.min(offset + BATCH_SIZE, currentLevel.size()));

                for (UUID parentUuid : batch) {
                    for (int c = 1; c <= CHILD_PER_NODE; c++) {
                        Folder created = folderService.createFolder(
                                TEST_PERF_USER_UUID, buildPerfRequest("d" + depth + "-" + c, parentUuid));
                        if (!isLastDepth) nextLevel.add(created.getUuid());
                        depthCreated++;
                    }
                }
            }

            totalCreated += depthCreated;
            if (!isLastDepth) currentLevel = nextLevel;
            System.out.printf("[Depth %2d] %,8d개 생성  (누적 %,11d개)%n", depth, depthCreated, totalCreated);
        }

        System.out.printf("%n전체 완료: %,d개%n", totalCreated);
    }

    @Test
    @Commit
    @DisplayName("[moveSubFolder] 서브폴더가 지정한 목적지 부모폴더 아래로 이동한다")
    void moveSubFolder_moves_subfolder_to_destination() {

        Folder destinationFolder = folderService.getSimpleFolder(TEST_MOVE_DEST_UUID);

        SubFolderMoveRequest request = new SubFolderMoveRequest();
        request.setTargetFolderUuid(TEST_MOVE_TARGET_UUID);
        request.setDestinationFolderUuid(TEST_MOVE_DEST_UUID);

        Folder result = folderService.moveSubFolder(TEST_MOVE_USER_ID, request);

        assertNotNull(result,                                                   "이동된 폴더 객체가 반환되어야 합니다");
        assertEquals(TEST_MOVE_TARGET_UUID, result.getUuid(),                   "반환된 폴더의 UUID 가 이동 대상 UUID 와 일치해야 합니다");
        assertEquals(destinationFolder.getFolderId(), result.getParentFolderId(), "parentFolderId 가 목적지 폴더의 folderId 로 변경되어야 합니다");
        assertEquals(destinationFolder.getFolderType(), result.getFolderType(), "이동 후 folderType 이 유지되어야 합니다");
        assertEquals(destinationFolder.getOwnerId(), result.getOwnerId(),       "이동 후 ownerId 가 유지되어야 합니다");
        assertTrue(result.getFolderIdPath().startsWith(destinationFolder.getFolderIdPath()),
                "이동된 폴더의 idPath 가 목적지 폴더의 idPath 로 시작해야 합니다");
    }

    @Test
    @Transactional
    @DisplayName("[moveSubFolder] target 과 destination 의 FolderType 이 다르면 BAD_REQUEST 예외가 발생한다")
    void moveSubFolder_throws_when_folder_type_mismatch() {

        SubFolderMoveRequest request = new SubFolderMoveRequest();
        request.setTargetFolderUuid(TEST_MOVE_TYPE_MISMATCH_TARGET_UUID);
        request.setDestinationFolderUuid(TEST_MOVE_TYPE_MISMATCH_DEST_UUID);

        GeneralCustomException ex = assertThrows(
                GeneralCustomException.class,
                () -> folderService.moveSubFolder(TEST_MOVE_TYPE_MISMATCH_USER_ID, request),
                "FolderType 이 다를 때 GeneralCustomException 이 발생해야 합니다"
        );

        assertEquals("FOLDER TYPE MISMATCH", ex.getMessage());
    }

    @Test
    @Transactional
    @DisplayName("[moveSubFolder] target 과 destination 의 Owner 가 다르면 BAD_REQUEST 예외가 발생한다")
    void moveSubFolder_throws_when_owner_mismatch() {

        SubFolderMoveRequest request = new SubFolderMoveRequest();
        request.setTargetFolderUuid(TEST_MOVE_OWNER_MISMATCH_TARGET_UUID);
        request.setDestinationFolderUuid(TEST_MOVE_OWNER_MISMATCH_DEST_UUID);

        GeneralCustomException ex = assertThrows(
                GeneralCustomException.class,
                () -> folderService.moveSubFolder(TEST_MOVE_OWNER_MISMATCH_USER_ID, request),
                "Owner 가 다를 때 GeneralCustomException 이 발생해야 합니다"
        );

        assertEquals("FOLDER OWNER CAN NOT BE CHANGED", ex.getMessage());
    }

    private FolderCreateRequest buildPerfRequest(String name, UUID parentUuid) {
        FolderCreateRequest req = new FolderCreateRequest();
        req.setName(name);
        req.setParentFolderUuid(parentUuid);
        req.setFolderType(TEST_PERF_FOLDER_TYPE);
        return req;
    }

}
