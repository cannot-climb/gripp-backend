package kr.njw.gripp.auth.repository;

import kr.njw.gripp.auth.entity.Account;
import kr.njw.gripp.auth.entity.AccountToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountTokenRepository extends JpaRepository<AccountToken, Long> {
    Optional<AccountToken> findByAccount(Account account);
}
