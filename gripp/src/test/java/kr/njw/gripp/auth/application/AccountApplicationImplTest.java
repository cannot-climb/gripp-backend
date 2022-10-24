package kr.njw.gripp.auth.application;

import kr.njw.gripp.auth.application.dto.LoginAppRequest;
import kr.njw.gripp.auth.application.dto.LoginAppResponse;
import kr.njw.gripp.auth.application.dto.SignUpAppRequest;
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

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void signUp() {
        this.setUpPasswordEncoder(true, true);

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
        this.setUpPasswordEncoder(true, true);
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
        this.setUpPasswordEncoder(true, true);
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
    }

    @Test
    void deleteRefreshToken() {
    }

    private void setUpPasswordEncoder(boolean encode, boolean matches) {
        if (encode) {
            given(this.passwordEncoder.encode(anyString())).willAnswer(invocation -> {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(invocation.getArgument(0).toString());
                stringBuilder.reverse();
                return stringBuilder.toString();
            });
        }

        if (matches) {
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
