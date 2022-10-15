package kr.njw.gripp.user.entity;

import kr.njw.gripp.article.entity.Article;
import lombok.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@ToString
public class User {
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

    public void noticeNewArticle(Article article) {
        if (article != null) {
            this.articleCount++;
            this.logger.info("새로운 게시물 발생 - " + this.username + ", " + article);
        }
    }

    public void noticeNewCertified(Article article) {
        if (article != null) {
            // TODO: score 증가
            this.articleCertifiedCount++;
            this.logger.info("새로운 등반 성공 발생 - " + this.username + ", " + article);
        }
    }
}
