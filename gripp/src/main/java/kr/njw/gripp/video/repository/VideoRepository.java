package kr.njw.gripp.video.repository;

import kr.njw.gripp.video.entity.Video;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VideoRepository extends JpaRepository<Video, Long> {
    Optional<Video> findByUuid(String uuid);
}
