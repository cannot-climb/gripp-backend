package kr.njw.gripp.auth.application;

import kr.njw.gripp.auth.application.dto.LoginAppRequest;
import kr.njw.gripp.auth.application.dto.LoginAppResponse;
import kr.njw.gripp.auth.application.dto.SignUpAppRequest;
import kr.njw.gripp.auth.entity.Account;
import kr.njw.gripp.auth.entity.AccountToken;
import kr.njw.gripp.auth.repository.AccountRepository;
import kr.njw.gripp.auth.repository.AccountTokenRepository;
import kr.njw.gripp.auth.util.PasswordUtil;
import kr.njw.gripp.global.auth.JwtAuthenticationProvider;
import kr.njw.gripp.global.auth.Role;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AccountApplicationImpl implements AccountApplication {
    private final AccountRepository accountRepository;
    private final AccountTokenRepository accountTokenRepository;
    private final PasswordUtil passwordUtil;
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
                .password(this.passwordUtil.encryptPassword(request.getPassword()))
                .registerDateTime(LocalDateTime.now())
                .build();

        this.accountRepository.save(account);
        return true;
    }

    @Transactional
    public LoginAppResponse login(LoginAppRequest request) {
        Optional<Account> account = this.accountRepository.findByUsername(request.getUsername());

        if (account.isEmpty() ||
                !this.passwordUtil.verifyPassword(request.getPassword(), account.get().getPassword())) {
            LoginAppResponse response = new LoginAppResponse();
            response.setSuccess(false);
            return response;
        }

        AccountToken accountToken = this.accountTokenRepository.findByAccount(account.get())
                .orElse(AccountToken.builder()
                        .account(account.get())
                        .build());

        accountToken.issue();
        this.accountTokenRepository.save(accountToken);

        LoginAppResponse response = new LoginAppResponse();
        response.setSuccess(true);
        response.setAccessToken(
                this.jwtAuthenticationProvider.createToken(account.get().getUsername(), List.of(Role.USER)));
        response.setRefreshToken(accountToken.getRefreshToken());
        return response;
    }

    public boolean isUsernameExisted(String username) {
        Optional<Account> account = this.accountRepository.findByUsername(username);
        return account.isPresent();
    }
}
