package kr.njw.gripp.video.repository;

import kr.njw.gripp.video.entity.Video;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.persistence.LockModeType;
import java.util.Optional;

public interface VideoRepository extends JpaRepository<Video, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select v from Video v where v.uuid = :uuid")
    Optional<Video> findByUuidForUpdate(@Param("uuid") String uuid);
}
