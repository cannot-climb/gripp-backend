package kr.njw.gripp.user.entity;

import kr.njw.gripp.article.entity.Article;
import lombok.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "user")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@ToString
public class User {
    public static final int ARTICLE_MAX_COUNT_FOR_COMPUTE_SCORE = 10;
    private static final int[] SCORE_WEIGHTS = {200, 200, 200, 100, 100, 50, 50, 50, 25, 25};

    @Transient
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(length = 100, nullable = false, unique = true)
    private String username;
    @Column(nullable = false)
    private int score;
    @Column(nullable = false)
    private int articleCount;
    @Column(nullable = false)
    private int articleCertifiedCount;
    @Column(nullable = false)
    private LocalDateTime registerDateTime;

    public int getTier() {
        return (this.score + 50) / 100;
    }

    public void incrementArticleCount() {
        this.articleCount++;
    }

    public void decrementArticleCount() {
        this.articleCount--;

        if (this.articleCount < 0) {
            this.articleCount = 0;
            this.logger.error("article count must be non negative");
        }
    }

    public void incrementArticleCertifiedCount() {
        this.articleCertifiedCount++;
    }

    public void decrementArticleCertifiedCount() {
        this.articleCertifiedCount--;

        if (this.articleCertifiedCount < 0) {
            this.articleCertifiedCount = 0;
            this.logger.error("article certified count must be non negative");
        }
    }

    public void submitScore(Collection<Article> bestArticlesForComputeScore) {
        List<Article> articles = bestArticlesForComputeScore.stream()
                .filter(article -> article.getVideo() != null && article.getVideo().isCertified())
                .sorted((a, b) -> b.getLevel() - a.getLevel()).toList();

        this.score = 0;

        for (int i = 0; i < Math.min(articles.size(), ARTICLE_MAX_COUNT_FOR_COMPUTE_SCORE); i++) {
            this.score += articles.get(i).getLevel() * SCORE_WEIGHTS[i];
        }

        this.score /= 10;
    }
}
