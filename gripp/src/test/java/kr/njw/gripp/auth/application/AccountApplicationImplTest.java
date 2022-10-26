package kr.njw.gripp.auth.application;

import kr.njw.gripp.auth.application.dto.*;
import kr.njw.gripp.auth.entity.Account;
import kr.njw.gripp.auth.entity.AccountToken;
import kr.njw.gripp.auth.repository.AccountRepository;
import kr.njw.gripp.auth.repository.AccountTokenRepository;
import kr.njw.gripp.global.auth.JwtAuthenticationProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class AccountApplicationImplTest {
    private static final String FIXED_JWT_TOKEN = "jwt";

    @InjectMocks
    private AccountApplicationImpl accountApplicationImpl;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private AccountTokenRepository accountTokenRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtAuthenticationProvider jwtAuthenticationProvider;
    private LocalDateTime beforeNow;

    @BeforeEach
    void setUp() {
        this.beforeNow = LocalDateTime.now().minusSeconds(5);
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void signUp() {
        this.setUpPasswordEncoder(true);

        SignUpAppRequest request = new SignUpAppRequest();
        request.setUsername("test1");
        request.setPassword("test2");

        SignUpAppRequest request2 = new SignUpAppRequest();
        request2.setUsername("test12");
        request2.setPassword("test2");

        given(this.accountRepository.findByUsername(any())).willReturn(Optional.empty());
        given(this.accountRepository.findByUsername(request.getUsername())).willReturn(
                Optional.of(Account.builder().build()));

        boolean result = this.accountApplicationImpl.signUp(request);
        boolean result2 = this.accountApplicationImpl.signUp(request2);

        then(this.accountRepository).should(times(1)).save(any());
        then(this.accountRepository).should(times(1))
                .save(argThat(argument -> argument.getUsername().equals(request2.getUsername()) &&
                        this.passwordEncoder.matches(request2.getPassword(), argument.getPassword())));

        assertThat(result).isFalse();
        assertThat(result2).isTrue();
    }

    @Test
    void login() {
        this.setUpPasswordEncoder(true);
        this.setUpJwtAuthenticationProvider();

        String rawPassword = "test23";
        Account account = Account.builder()
                .username("test32")
                .password(this.passwordEncoder.encode(rawPassword))
                .build();

        LoginAppRequest request = new LoginAppRequest();
        request.setUsername("test1");
        request.setPassword("test2");

        LoginAppRequest request2 = new LoginAppRequest();
        request2.setUsername(account.getUsername());
        request2.setPassword("test2");

        LoginAppRequest request3 = new LoginAppRequest();
        request3.setUsername(account.getUsername());
        request3.setPassword(rawPassword);

        given(this.accountRepository.findByUsername(any())).willReturn(Optional.empty());
        given(this.accountRepository.findByUsername(account.getUsername())).willReturn(Optional.of(account));

        LoginAppResponse response = this.accountApplicationImpl.login(request);
        LoginAppResponse response2 = this.accountApplicationImpl.login(request2);
        LoginAppResponse response3 = this.accountApplicationImpl.login(request3);

        then(this.accountTokenRepository).should(times(1)).save(any());
        then(this.accountTokenRepository).should(times(1))
                .save(argThat(argument ->
                        argument.getRefreshToken().equals(response3.getRefreshToken().orElseThrow())));

        assertThat(response.isSuccess()).isFalse();
        assertThat(response2.isSuccess()).isFalse();
        assertThat(response3.isSuccess()).isTrue();
        assertThat(response3.getAccessToken().orElseThrow()).isEqualTo(FIXED_JWT_TOKEN);
    }

    @Test
    void loginShouldIssue() {
        this.setUpPasswordEncoder(true);
        this.setUpJwtAuthenticationProvider();

        String rawPassword = "a";
        Account account = Account.builder()
                .username("a")
                .password(this.passwordEncoder.encode(rawPassword))
                .build();
        AccountToken accountToken = spy(AccountToken.builder().account(account).build());

        LoginAppRequest request = new LoginAppRequest();
        request.setUsername(account.getUsername());
        request.setPassword(rawPassword);

        given(this.accountRepository.findByUsername(account.getUsername())).willReturn(Optional.of(account));
        given(this.accountTokenRepository.findByAccount(any())).willReturn(Optional.of(accountToken));

        LoginAppResponse response = this.accountApplicationImpl.login(request);

        then(accountToken).should(times(1)).issue();
        then(this.accountTokenRepository).should(times(1)).save(any());
        then(this.accountTokenRepository).should(times(1))
                .save(argThat(argument ->
                        argument.getRefreshToken().equals(response.getRefreshToken().orElseThrow())));

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getAccessToken().orElseThrow()).isEqualTo(FIXED_JWT_TOKEN);
    }

    @Test
    void refreshToken() {
        this.setUpPasswordEncoder(false);
        this.setUpJwtAuthenticationProvider();

        String rawPassword = "asdada";

        Account account = Account.builder()
                .username("asda")
                .password(this.passwordEncoder.encode(rawPassword))
                .build();

        Account account2 = Account.builder()
                .username("asda2")
                .password(this.passwordEncoder.encode(rawPassword))
                .build();

        Account account3 = Account.builder()
                .username("asda3")
                .password(this.passwordEncoder.encode(rawPassword))
                .build();

        AccountToken accountToken = spy(AccountToken.builder()
                .account(account2)
                .refreshToken("ref")
                .expireDateTime(this.beforeNow)
                .build());

        AccountToken accountToken2 = spy(AccountToken.builder()
                .account(account3)
                .refreshToken("ref2")
                .expireDateTime(this.beforeNow.plusMinutes(1))
                .build());

        RefreshTokenAppRequest request = new RefreshTokenAppRequest();
        request.setUsername("no");
        request.setRefreshToken("tok");

        RefreshTokenAppRequest request2 = new RefreshTokenAppRequest();
        request2.setUsername(account.getUsername());
        request2.setRefreshToken("tok");

        RefreshTokenAppRequest request3 = new RefreshTokenAppRequest();
        request3.setUsername(account2.getUsername());
        request3.setRefreshToken("tok");

        RefreshTokenAppRequest request4 = new RefreshTokenAppRequest();
        request4.setUsername(account2.getUsername());
        request4.setRefreshToken(accountToken.getRefreshToken());

        RefreshTokenAppRequest request5 = new RefreshTokenAppRequest();
        request5.setUsername(account3.getUsername());
        request5.setRefreshToken(accountToken2.getRefreshToken());

        given(this.accountRepository.findByUsername(anyString())).willReturn(Optional.empty());
        given(this.accountRepository.findByUsername(account.getUsername())).willReturn(Optional.of(account));
        given(this.accountRepository.findByUsername(account2.getUsername())).willReturn(Optional.of(account2));
        given(this.accountRepository.findByUsername(account3.getUsername())).willReturn(Optional.of(account3));
        given(this.accountTokenRepository.findByAccount(any())).willReturn(Optional.empty());
        given(this.accountTokenRepository.findByAccount(account2)).willReturn(Optional.of(accountToken));
        given(this.accountTokenRepository.findByAccount(account3)).willReturn(Optional.of(accountToken2));

        RefreshTokenAppResponse response = this.accountApplicationImpl.refreshToken(request);
        RefreshTokenAppResponse response2 = this.accountApplicationImpl.refreshToken(request2);
        RefreshTokenAppResponse response3 = this.accountApplicationImpl.refreshToken(request3);
        RefreshTokenAppResponse response4 = this.accountApplicationImpl.refreshToken(request4);
        RefreshTokenAppResponse response5 = this.accountApplicationImpl.refreshToken(request5);

        then(accountToken2).should(times(1)).rotate();
        then(this.accountTokenRepository).should(times(1)).save(any());
        then(this.accountTokenRepository).should(times(1))
                .save(argThat(argument ->
                        argument.getRefreshToken().equals(response5.getRefreshToken().orElseThrow())));

        assertThat(response.isSuccess()).isFalse();
        assertThat(response2.isSuccess()).isFalse();
        assertThat(response3.isSuccess()).isFalse();
        assertThat(response4.isSuccess()).isFalse();
        assertThat(response5.isSuccess()).isTrue();
        assertThat(response5.getAccessToken().orElseThrow()).isEqualTo(FIXED_JWT_TOKEN);
    }

    @Test
    void deleteRefreshToken() {
        this.setUpPasswordEncoder(false);

        String rawPassword = "테스트";

        Account account = Account.builder()
                .username("아이디")
                .password(this.passwordEncoder.encode(rawPassword))
                .build();

        Account account2 = Account.builder()
                .username("아이디2")
                .password(this.passwordEncoder.encode(rawPassword))
                .build();

        Account account3 = Account.builder()
                .username("아이디3")
                .password(this.passwordEncoder.encode(rawPassword))
                .build();

        AccountToken accountToken = spy(AccountToken.builder()
                .account(account2)
                .refreshToken("토큰")
                .expireDateTime(this.beforeNow)
                .build());

        AccountToken accountToken2 = spy(AccountToken.builder()
                .account(account3)
                .refreshToken("토큰2")
                .expireDateTime(this.beforeNow.plusMinutes(1))
                .build());

        DeleteRefreshTokenAppRequest request = new DeleteRefreshTokenAppRequest();
        request.setUsername("없음");
        request.setRefreshToken("token");

        DeleteRefreshTokenAppRequest request2 = new DeleteRefreshTokenAppRequest();
        request2.setUsername(account.getUsername());
        request2.setRefreshToken("token");

        DeleteRefreshTokenAppRequest request3 = new DeleteRefreshTokenAppRequest();
        request3.setUsername(account2.getUsername());
        request3.setRefreshToken("token");

        DeleteRefreshTokenAppRequest request4 = new DeleteRefreshTokenAppRequest();
        request4.setUsername(account2.getUsername());
        request4.setRefreshToken(accountToken.getRefreshToken());

        DeleteRefreshTokenAppRequest request5 = new DeleteRefreshTokenAppRequest();
        request5.setUsername(account3.getUsername());
        request5.setRefreshToken(accountToken2.getRefreshToken());

        given(this.accountRepository.findByUsername(anyString())).willReturn(Optional.empty());
        given(this.accountRepository.findByUsername(account.getUsername())).willReturn(Optional.of(account));
        given(this.accountRepository.findByUsername(account2.getUsername())).willReturn(Optional.of(account2));
        given(this.accountRepository.findByUsername(account3.getUsername())).willReturn(Optional.of(account3));
        given(this.accountTokenRepository.findByAccount(any())).willReturn(Optional.empty());
        given(this.accountTokenRepository.findByAccount(account2)).willReturn(Optional.of(accountToken));
        given(this.accountTokenRepository.findByAccount(account3)).willReturn(Optional.of(accountToken2));

        boolean response = this.accountApplicationImpl.deleteRefreshToken(request);
        boolean response2 = this.accountApplicationImpl.deleteRefreshToken(request2);
        boolean response3 = this.accountApplicationImpl.deleteRefreshToken(request3);
        boolean response4 = this.accountApplicationImpl.deleteRefreshToken(request4);
        boolean response5 = this.accountApplicationImpl.deleteRefreshToken(request5);

        then(this.accountTokenRepository).should(times(1)).delete(any());
        then(this.accountTokenRepository).should(times(1)).delete(accountToken2);

        assertThat(response).isFalse();
        assertThat(response2).isFalse();
        assertThat(response3).isFalse();
        assertThat(response4).isFalse();
        assertThat(response5).isTrue();
    }

    private void setUpPasswordEncoder(boolean useMatches) {
        given(this.passwordEncoder.encode(anyString())).willAnswer(invocation -> {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(invocation.getArgument(0).toString());
            stringBuilder.reverse();
            return stringBuilder.toString();
        });

        if (useMatches) {
            given(this.passwordEncoder.matches(anyString(), anyString())).willAnswer(invocation -> {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(invocation.getArgument(0).toString());
                stringBuilder.reverse();
                return stringBuilder.toString().equals(invocation.getArgument(1).toString());
            });
        }
    }

    private void setUpJwtAuthenticationProvider() {
        given(this.jwtAuthenticationProvider.createToken(any(), any())).willReturn(FIXED_JWT_TOKEN);
    }
}
