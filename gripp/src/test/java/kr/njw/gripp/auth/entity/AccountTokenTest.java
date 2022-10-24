package kr.njw.gripp.auth.entity;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

class AccountTokenTest {
    private LocalDateTime beforeNow;

    @BeforeEach
    void setUp() {
        this.beforeNow = LocalDateTime.now().minusSeconds(5);
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void rotate() {
        AccountToken accountToken = AccountToken.builder().build();
        AccountToken accountToken2 = AccountToken.builder()
                .account(Account.builder().build())
                .refreshToken("old")
                .registerDateTime(this.beforeNow)
                .expireDateTime(this.beforeNow)
                .build();

        Throwable throwable = catchThrowable(accountToken::rotate);
        accountToken2.rotate();

        assertThat(throwable).isInstanceOf(RuntimeException.class);
        assertThat(throwable.getMessage()).contains("no account");

        assertThat(accountToken2.getRefreshToken()).hasSize(64);
        assertThat(StringUtils.isAlphanumeric(accountToken2.getRefreshToken())).isTrue();
        assertThat(accountToken2.getRegisterDateTime()).isAfter(this.beforeNow);
        assertThat(accountToken2.getExpireDateTime()).isEqualTo(this.beforeNow);
    }

    @Test
    void issue() {
        AccountToken accountToken = AccountToken.builder().build();
        AccountToken accountToken2 = AccountToken.builder()
                .account(Account.builder().build())
                .build();

        Throwable throwable = catchThrowable(accountToken::issue);
        accountToken2.issue();

        assertThat(throwable).isInstanceOf(RuntimeException.class);
        assertThat(throwable.getMessage()).contains("no account");

        assertThat(accountToken2.getRefreshToken()).hasSize(64);
        assertThat(StringUtils.isAlphanumeric(accountToken2.getRefreshToken())).isTrue();
        assertThat(accountToken2.getRegisterDateTime()).isAfter(this.beforeNow);
        assertThat(accountToken2.getExpireDateTime()).isEqualTo(accountToken2.getRegisterDateTime().plusDays(30));
    }

    @Test
    void isExpired() {
        AccountToken accountToken = AccountToken.builder().expireDateTime(this.beforeNow).build();
        AccountToken accountToken2 = AccountToken.builder().expireDateTime(LocalDateTime.now().plusSeconds(5)).build();

        boolean expired = accountToken.isExpired();
        boolean expired2 = accountToken2.isExpired();

        assertThat(expired).isTrue();
        assertThat(expired2).isFalse();
    }
}
