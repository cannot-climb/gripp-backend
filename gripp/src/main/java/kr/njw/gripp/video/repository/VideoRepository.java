package kr.njw.gripp.video.repository;

import kr.njw.gripp.video.entity.Video;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import javax.persistence.LockModeType;
import java.util.Optional;

public interface VideoRepository extends JpaRepository<Video, Long> {
    Optional<Video> findByUuid(String uuid);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Video> findForUpdateByUuid(String uuid);

    @Lock(LockModeType.PESSIMISTIC_READ)
    Optional<Video> findForShareByUuid(String uuid);
}
