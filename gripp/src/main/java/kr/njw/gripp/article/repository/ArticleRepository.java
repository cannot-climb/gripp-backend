package kr.njw.gripp.article.repository;

import kr.njw.gripp.article.entity.Article;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.persistence.LockModeType;
import java.util.Optional;

public interface ArticleRepository extends JpaRepository<Article, Long> {
    boolean existsByVideoId(Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from Article a where a.video.id = :id")
    Optional<Article> findByVideoIdForUpdate(@Param("id") Long id);
}
