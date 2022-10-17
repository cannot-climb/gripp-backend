package kr.njw.gripp.article.repository;

import kr.njw.gripp.article.entity.Article;
import kr.njw.gripp.video.entity.vo.VideoStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import javax.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

public interface ArticleRepository extends JpaRepository<Article, Long> {
    boolean existsByVideoId(Long id);

    @Override
    @EntityGraph(attributePaths = {"user", "video"}, type = EntityGraph.EntityGraphType.LOAD)
    Optional<Article> findById(Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @EntityGraph(attributePaths = {"user", "video"}, type = EntityGraph.EntityGraphType.LOAD)
    Optional<Article> findForUpdateById(Long id);

    @Lock(LockModeType.PESSIMISTIC_READ)
    @EntityGraph(attributePaths = {"user", "video"}, type = EntityGraph.EntityGraphType.LOAD)
    Optional<Article> findForShareByVideoId(Long videoId);

    @Lock(LockModeType.PESSIMISTIC_READ)
    @EntityGraph(attributePaths = {"user", "video"}, type = EntityGraph.EntityGraphType.LOAD)
    List<Article> findForShareByUserIdAndVideoStatusOrderByLevelDesc(Long userId, VideoStatus status,
                                                                     Pageable pageable);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Article a set a.viewCount = a.viewCount + 1 where a.id = :id")
    void incrementViewCountById(@Param("id") Long id);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Article a set a.favoriteCount = a.favoriteCount + 1 where a.id = :id")
    void incrementFavoriteCountById(@Param("id") Long id);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Article a set a.favoriteCount = a.favoriteCount - 1 where a.id = :id and a.favoriteCount > 0")
    void decrementFavoriteCountById(@Param("id") Long id);
}
