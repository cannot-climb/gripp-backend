package kr.njw.gripp.article.repository;

import kr.njw.gripp.article.entity.ArticleFavorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import javax.persistence.LockModeType;
import java.util.Optional;

public interface ArticleFavoriteRepository extends JpaRepository<ArticleFavorite, Long> {
    boolean existsByArticleIdAndUserId(Long articleId, Long userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<ArticleFavorite> findForUpdateByArticleIdAndUserId(Long articleId, Long userId);
}
