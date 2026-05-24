package com.uruz.frostbite.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PermissionBitMaskTest {

    @Test
    @DisplayName("of는 여러 권한을 하나의 integer bitmask로 합친다")
    void of_combines_permissions_to_integer_bitmask() {
        // given & when
        int mask = PermissionBitMask.of(
                PermissionBitMask.BROWSE,
                PermissionBitMask.READ,
                PermissionBitMask.WRITE
        );

        // then
        assertThat(mask).isEqualTo(7);
    }

    @Test
    @DisplayName("has는 bitmask에 특정 권한이 포함되어 있는지 검사한다")
    void has_returns_whether_mask_contains_permission() {
        // given
        int mask = PermissionBitMask.of(PermissionBitMask.BROWSE, PermissionBitMask.READ);

        // when & then
        assertThat(PermissionBitMask.has(mask, PermissionBitMask.BROWSE)).isTrue();
        assertThat(PermissionBitMask.has(mask, PermissionBitMask.READ)).isTrue();
        assertThat(PermissionBitMask.has(mask, PermissionBitMask.WRITE)).isFalse();
    }

    @Test
    @DisplayName("NONE은 실제 권한이 아니므로 has 검사에서 true가 되지 않는다")
    void has_returns_false_for_none_permission() {
        // given
        int mask = PermissionBitMask.of(PermissionBitMask.BROWSE, PermissionBitMask.READ);

        // when & then
        assertThat(PermissionBitMask.has(mask, PermissionBitMask.NONE)).isFalse();
        assertThat(PermissionBitMask.has(0, PermissionBitMask.NONE)).isFalse();
    }

    @Test
    @DisplayName("hasAll은 요청한 모든 권한이 포함될 때만 true를 반환한다")
    void hasAll_returns_true_only_when_all_permissions_are_included() {
        // given
        int mask = PermissionBitMask.of(PermissionBitMask.BROWSE, PermissionBitMask.READ);

        // when & then
        assertThat(PermissionBitMask.hasAll(mask, PermissionBitMask.BROWSE, PermissionBitMask.READ)).isTrue();
        assertThat(PermissionBitMask.hasAll(mask, PermissionBitMask.BROWSE, PermissionBitMask.WRITE)).isFalse();
    }

    @Test
    @DisplayName("hasAny는 요청한 권한 중 하나라도 포함되면 true를 반환한다")
    void hasAny_returns_true_when_any_permission_is_included() {
        // given
        int mask = PermissionBitMask.of(PermissionBitMask.BROWSE, PermissionBitMask.READ);

        // when & then
        assertThat(PermissionBitMask.hasAny(mask, PermissionBitMask.WRITE, PermissionBitMask.READ)).isTrue();
        assertThat(PermissionBitMask.hasAny(mask, PermissionBitMask.WRITE, PermissionBitMask.CONTROL)).isFalse();
    }

    @Test
    @DisplayName("fromMaskToArray는 integer bitmask를 권한 배열로 복원한다")
    void fromMaskToArray_restores_permissions_from_integer_bitmask() {
        // given
        int mask = PermissionBitMask.of(PermissionBitMask.BROWSE, PermissionBitMask.MANAGE);

        // when
        PermissionBitMask[] permissions = PermissionBitMask.fromMaskToArray(mask);

        // then
        assertThat(permissions)
                .containsExactly(PermissionBitMask.BROWSE, PermissionBitMask.MANAGE);
    }

    @Test
    @DisplayName("fromMaskToArray는 null 또는 0을 빈 배열로 복원한다")
    void fromMaskToArray_returns_empty_array_for_null_or_zero() {
        // when & then
        assertThat(PermissionBitMask.fromMaskToArray(null)).isEmpty();
        assertThat(PermissionBitMask.fromMaskToArray(0)).isEmpty();
    }

    @Test
    @DisplayName("allMasks는 NONE을 제외한 실제 권한만 반환한다")
    void allMasks_returns_real_permissions_only() {
        // when
        PermissionBitMask[] permissions = PermissionBitMask.allMasks();

        // then
        assertThat(permissions)
                .containsExactly(
                        PermissionBitMask.BROWSE,
                        PermissionBitMask.READ,
                        PermissionBitMask.WRITE,
                        PermissionBitMask.CONTROL,
                        PermissionBitMask.MANAGE
                );
    }

    @Test
    @DisplayName("fromValue는 알 수 없는 값을 받으면 예외를 던진다")
    void fromValue_throws_exception_for_unknown_value() {
        // when & then
        assertThatThrownBy(() -> PermissionBitMask.fromValue(999))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unknown PermissionBitMask value");
    }
}
