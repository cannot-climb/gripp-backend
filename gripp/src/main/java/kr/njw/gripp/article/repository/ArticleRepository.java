package kr.njw.gripp.article.repository;

import kr.njw.gripp.article.entity.Article;
import kr.njw.gripp.video.entity.vo.VideoStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

public interface ArticleRepository extends JpaRepository<Article, Long> {
    boolean existsByVideoId(Long id);

    @Lock(LockModeType.PESSIMISTIC_READ)
    @Query("select a from Article a join fetch a.user join fetch a.video where a.video.id = :id")
    Optional<Article> findByVideoIdWithReadLock(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_READ)
    @Query("""
            select a from Article a join fetch a.user join fetch a.video
            where a.user.id = :userId and a.video.status = :videoStatus order by a.level desc""")
    List<Article> findTopWithReadLock(@Param("userId") Long userId, @Param("videoStatus") VideoStatus status,
                                      Pageable pageable);
}
