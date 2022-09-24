package kr.njw.gripp.auth.application;

import kr.njw.gripp.auth.controller.dto.SignUpRequest;
import kr.njw.gripp.auth.entity.Account;
import kr.njw.gripp.auth.repository.AccountRepository;
import kr.njw.gripp.auth.util.PasswordUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AccountApplication {
    private final AccountRepository accountRepository;
    private final PasswordUtil passwordUtil;

    public boolean signUp(SignUpRequest request) {
        if (this.isUsernameExisted(request.getUsername())) {
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

    public boolean isUsernameExisted(String username) {
        Optional<Account> account = this.accountRepository.findByUsername(username);
        return account.isPresent();
    }
}
