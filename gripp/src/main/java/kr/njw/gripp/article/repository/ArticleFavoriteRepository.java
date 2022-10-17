package kr.njw.gripp.article.repository;

import kr.njw.gripp.article.entity.ArticleFavorite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ArticleFavoriteRepository extends JpaRepository<ArticleFavorite, Long> {
    boolean existsByArticleIdAndUserId(Long articleId, Long userId);

    Optional<ArticleFavorite> findByArticleIdAndUserId(Long articleId, Long userId);
}
