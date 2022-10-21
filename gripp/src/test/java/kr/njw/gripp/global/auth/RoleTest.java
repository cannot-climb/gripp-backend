package kr.njw.gripp.global.auth;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RoleTest {
    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void getValue() {
        assertThat(Role.ADMIN.getValue()).isEqualTo("ADMIN");
        assertThat(Role.USER.getValue()).isEqualTo("USER");
    }

    @Test
    void toAuthority() {
        assertThat(Role.ADMIN.toAuthority()).isEqualTo("ROLE_ADMIN");
        assertThat(Role.USER.toAuthority()).isEqualTo("ROLE_USER");
    }
}
