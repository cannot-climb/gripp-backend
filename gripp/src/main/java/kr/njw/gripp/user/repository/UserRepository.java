package kr.njw.gripp.user.repository;

import kr.njw.gripp.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import javax.persistence.LockModeType;
import java.util.Optional;
import java.util.stream.Stream;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    long countByScoreGreaterThan(int score);

    Stream<User> findByOrderByScoreDescIdAsc();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<User> findForUpdateById(Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<User> findForUpdateByUsername(String username);
}
