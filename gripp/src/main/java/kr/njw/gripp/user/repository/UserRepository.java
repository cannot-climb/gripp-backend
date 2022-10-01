package kr.njw.gripp.user.repository;

import kr.njw.gripp.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.stream.Stream;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    long countByScoreGreaterThan(int score);

    Stream<User> findAllByOrderByScoreDescIdAsc();
}