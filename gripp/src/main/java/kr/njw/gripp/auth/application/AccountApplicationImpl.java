package kr.njw.gripp.auth.application;

import kr.njw.gripp.auth.application.dto.*;
import kr.njw.gripp.auth.entity.Account;
import kr.njw.gripp.auth.entity.AccountToken;
import kr.njw.gripp.auth.repository.AccountRepository;
import kr.njw.gripp.auth.repository.AccountTokenRepository;
import kr.njw.gripp.global.auth.JwtAuthenticationProvider;
import kr.njw.gripp.global.auth.Role;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class AccountApplicationImpl implements AccountApplication {
    private final AccountRepository accountRepository;
    private final AccountTokenRepository accountTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtAuthenticationProvider jwtAuthenticationProvider;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Transactional
    public boolean signUp(SignUpAppRequest request) {
        if (this.isUsernameExisted(request.getUsername())) {
            this.logger.warn("아이디가 이미 존재합니다 - " + request.getUsername());
            return false;
        }

        Account account = Account.builder()
                .username(request.getUsername())
                .password(this.passwordEncoder.encode(request.getPassword()))
                .registerDateTime(LocalDateTime.now())
                .build();

        this.accountRepository.save(account);
        return true;
    }

    @Transactional
    public LoginAppResponse login(LoginAppRequest request) {
        Optional<Account> account = this.accountRepository.findByUsername(request.getUsername());

        if (account.isEmpty() || !this.passwordEncoder.matches(request.getPassword(), account.get().getPassword())) {
            LoginAppResponse response = new LoginAppResponse();
            response.setSuccess(false);
            return response;
        }

        AccountToken accountToken = this.accountTokenRepository.findByAccount(account.get())
                .orElse(AccountToken.builder().account(account.get()).build());

        accountToken.issue();
        this.accountTokenRepository.save(accountToken);

        LoginAppResponse response = new LoginAppResponse();
        response.setSuccess(true);
        response.setAccessToken(
                this.jwtAuthenticationProvider.createToken(account.get().getUsername(), List.of(Role.USER)));
        response.setRefreshToken(accountToken.getRefreshToken());
        return response;
    }

    @Transactional
    public RefreshTokenAppResponse refreshToken(RefreshTokenAppRequest request) {
        Optional<Account> account = this.accountRepository.findByUsername(request.getUsername());

        if (account.isEmpty()) {
            RefreshTokenAppResponse response = new RefreshTokenAppResponse();
            response.setSuccess(false);
            this.logger.warn("회원이 존재하지 않습니다 - " + request.getUsername());
            return response;
        }

        Optional<AccountToken> accountToken = this.accountTokenRepository.findByAccount(account.get());

        if (accountToken.isEmpty() || !accountToken.get().getRefreshToken().equals(request.getRefreshToken())) {
            RefreshTokenAppResponse response = new RefreshTokenAppResponse();
            response.setSuccess(false);
            this.logger.warn("리프레시 토큰이 올바르지 않습니다 - " + request.getRefreshToken());
            return response;
        }

        if (accountToken.get().isExpired()) {
            RefreshTokenAppResponse response = new RefreshTokenAppResponse();
            response.setSuccess(false);
            return response;
        }

        accountToken.get().rotate();
        this.accountTokenRepository.save(accountToken.get());

        RefreshTokenAppResponse response = new RefreshTokenAppResponse();
        response.setSuccess(true);
        response.setAccessToken(
                this.jwtAuthenticationProvider.createToken(account.get().getUsername(), List.of(Role.USER)));
        response.setRefreshToken(accountToken.get().getRefreshToken());
        return response;
    }

    @Transactional
    public boolean deleteRefreshToken(DeleteRefreshTokenAppRequest request) {
        Optional<Account> account = this.accountRepository.findByUsername(request.getUsername());

        if (account.isEmpty()) {
            this.logger.warn("회원이 존재하지 않습니다 - " + request.getUsername());
            return false;
        }

        Optional<AccountToken> accountToken = this.accountTokenRepository.findByAccount(account.get());

        if (accountToken.isEmpty() || !accountToken.get().getRefreshToken().equals(request.getRefreshToken())) {
            this.logger.warn("리프레시 토큰이 올바르지 않습니다 - " + request.getRefreshToken());
            return false;
        }

        if (accountToken.get().isExpired()) {
            return false;
        }

        this.accountTokenRepository.delete(accountToken.get());
        return true;
    }

    public boolean isUsernameExisted(String username) {
        Optional<Account> account = this.accountRepository.findByUsername(username);
        return account.isPresent();
    }
}
