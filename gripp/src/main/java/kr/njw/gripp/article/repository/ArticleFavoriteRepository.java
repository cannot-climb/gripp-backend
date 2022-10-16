package kr.njw.gripp.article.repository;

import kr.njw.gripp.article.entity.ArticleFavorite;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArticleFavoriteRepository extends JpaRepository<ArticleFavorite, Long> {
    boolean existsByArticleIdAndUserId(Long articleId, Long userId);
}
