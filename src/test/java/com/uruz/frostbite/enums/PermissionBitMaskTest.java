package com.uruz.frostbite.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("PermissionBitMask 단위 테스트")
@ActiveProfiles("LOCAL-DEV-WIN")
@SpringBootTest(properties = {
        "spring.config.import=file:./application-secret.properties"
})
class PermissionBitMaskTest {

    @Test
    @DisplayName("여러 권한을 하나의 정수 비트마스크로 합친다")
    void combinesPermissions() {
        // when
        int mask = PermissionBitMask.of(
                PermissionBitMask.BROWSE,
                PermissionBitMask.READ,
                PermissionBitMask.WRITE
        );

        // then
        assertThat(mask).isEqualTo(7);
    }

    @Test
    @DisplayName("비트마스크에 특정 권한이 포함되어 있는지 판별한다")
    void checksSinglePermission() {
        // given
        int mask = PermissionBitMask.of(PermissionBitMask.BROWSE, PermissionBitMask.READ);

        // when & then
        assertThat(PermissionBitMask.has(mask, PermissionBitMask.BROWSE)).isTrue();
        assertThat(PermissionBitMask.has(mask, PermissionBitMask.READ)).isTrue();
        assertThat(PermissionBitMask.has(mask, PermissionBitMask.WRITE)).isFalse();
    }

    @Test
    @DisplayName("NONE은 실제 권한이 아니므로 항상 포함되지 않은 것으로 판별한다")
    void noneIsNeverIncluded() {
        assertThat(PermissionBitMask.has(PermissionBitMask.of(PermissionBitMask.BROWSE), PermissionBitMask.NONE)).isFalse();
        assertThat(PermissionBitMask.has(0, PermissionBitMask.NONE)).isFalse();
    }

    @Test
    @DisplayName("요청한 모든 권한이 포함된 경우에만 hasAll이 참이다")
    void checksAllPermissions() {
        // given
        int mask = PermissionBitMask.of(PermissionBitMask.BROWSE, PermissionBitMask.READ);

        // when & then
        assertThat(PermissionBitMask.hasAll(mask, PermissionBitMask.BROWSE, PermissionBitMask.READ)).isTrue();
        assertThat(PermissionBitMask.hasAll(mask, PermissionBitMask.BROWSE, PermissionBitMask.WRITE)).isFalse();
    }

    @Test
    @DisplayName("요청한 권한 중 하나라도 포함되면 hasAny가 참이다")
    void checksAnyPermission() {
        // given
        int mask = PermissionBitMask.of(PermissionBitMask.BROWSE, PermissionBitMask.READ);

        // when & then
        assertThat(PermissionBitMask.hasAny(mask, PermissionBitMask.WRITE, PermissionBitMask.READ)).isTrue();
        assertThat(PermissionBitMask.hasAny(mask, PermissionBitMask.WRITE, PermissionBitMask.CONTROL)).isFalse();
    }

    @Test
    @DisplayName("정수 비트마스크를 권한 배열로 복원한다")
    void restoresPermissionsFromMask() {
        // given
        int mask = PermissionBitMask.of(PermissionBitMask.BROWSE, PermissionBitMask.MANAGE);

        // when
        PermissionBitMask[] permissions = PermissionBitMask.fromMaskToArray(mask);

        // then
        assertThat(permissions).containsExactly(PermissionBitMask.BROWSE, PermissionBitMask.MANAGE);
    }

    @Test
    @DisplayName("null과 0은 빈 권한 배열로 복원한다")
    void restoresEmptyPermissionsFromNullOrZero() {
        assertThat(PermissionBitMask.fromMaskToArray(null)).isEmpty();
        assertThat(PermissionBitMask.fromMaskToArray(0)).isEmpty();
    }

    @Test
    @DisplayName("allMasks는 NONE을 제외한 실제 권한만 반환한다")
    void returnsAllRealPermissions() {
        assertThat(PermissionBitMask.allMasks()).containsExactly(
                PermissionBitMask.BROWSE,
                PermissionBitMask.READ,
                PermissionBitMask.WRITE,
                PermissionBitMask.CONTROL,
                PermissionBitMask.MANAGE
        );
    }

    @Test
    @DisplayName("정의되지 않은 값으로 enum을 조회하면 예외가 발생한다")
    void rejectsUnknownValue() {
        assertThatThrownBy(() -> PermissionBitMask.fromValue(999))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unknown PermissionBitMask value");
    }
}
