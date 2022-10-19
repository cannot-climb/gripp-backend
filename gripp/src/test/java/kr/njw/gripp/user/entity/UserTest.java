package kr.njw.gripp.user.entity;

import kr.njw.gripp.article.entity.Article;
import kr.njw.gripp.video.entity.Video;
import kr.njw.gripp.video.entity.vo.VideoStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class UserTest {

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void getTier() {
        User user = User.builder().score(0).build();
        User user2 = User.builder().score(50).build();
        User user3 = User.builder().score(1149).build();
        User user4 = User.builder().score(1200).build();
        User user5 = User.builder().score(1899).build();

        int tier = user.getTier();
        int tier2 = user2.getTier();
        int tier3 = user3.getTier();
        int tier4 = user4.getTier();
        int tier5 = user5.getTier();

        assertThat(tier).isEqualTo(0);
        assertThat(tier2).isEqualTo(1);
        assertThat(tier3).isEqualTo(11);
        assertThat(tier4).isEqualTo(12);
        assertThat(tier5).isEqualTo(19);
    }

    @Test
    void incrementArticleCount() {
        User user = User.builder().articleCount(0).build();

        user.incrementArticleCount();
        int articleCount = user.getArticleCount();
        user.incrementArticleCount();
        int articleCount2 = user.getArticleCount();

        assertThat(articleCount).isEqualTo(1);
        assertThat(articleCount2).isEqualTo(2);
    }

    @Test
    void decrementArticleCount() {
        User user = User.builder().articleCount(1).build();

        user.decrementArticleCount();
        int articleCount = user.getArticleCount();
        user.decrementArticleCount();
        int articleCount2 = user.getArticleCount();

        assertThat(articleCount).isEqualTo(0);
        assertThat(articleCount2).isEqualTo(0);
    }

    @Test
    void incrementArticleCertifiedCount() {
        User user = User.builder().articleCertifiedCount(0).build();

        user.incrementArticleCertifiedCount();
        int articleCertifiedCount = user.getArticleCertifiedCount();
        user.incrementArticleCertifiedCount();
        int articleCertifiedCount2 = user.getArticleCertifiedCount();

        assertThat(articleCertifiedCount).isEqualTo(1);
        assertThat(articleCertifiedCount2).isEqualTo(2);
    }

    @Test
    void decrementArticleCertifiedCount() {
        User user = User.builder().articleCertifiedCount(1).build();

        user.decrementArticleCertifiedCount();
        int articleCertifiedCount = user.getArticleCertifiedCount();
        user.decrementArticleCertifiedCount();
        int articleCertifiedCount2 = user.getArticleCertifiedCount();

        assertThat(articleCertifiedCount).isEqualTo(0);
        assertThat(articleCertifiedCount2).isEqualTo(0);
    }

    @Test
    void submitScore() {
        User user = User.builder().build();
        List<Article> articles = new ArrayList<>();

        for (int i = 0; i < 60; i++) {
            articles.add(Article.builder()
                    .level(i % 20)
                    .video(Video.builder()
                            .status((i % 20 != 18) ? VideoStatus.CERTIFIED : VideoStatus.NO_CERTIFIED)
                            .build())
                    .build());
        }

        user.submitScore(new ArrayList<>());
        int score = user.getScore();

        user.submitScore(List.of(Article.builder()
                .level(19)
                .video(Video.builder().status(VideoStatus.PREPROCESSING).build())
                .build()));
        int score2 = user.getScore();

        user.submitScore(List.of(Article.builder()
                .level(2)
                .video(Video.builder().status(VideoStatus.NO_CERTIFIED).build())
                .build()));
        int score3 = user.getScore();

        user.submitScore(List.of(Article.builder()
                .level(1)
                .video(Video.builder().status(VideoStatus.CERTIFIED).build())
                .build()));
        int score4 = user.getScore();

        user.submitScore(List.of(
                Article.builder()
                        .level(1)
                        .video(Video.builder().status(VideoStatus.CERTIFIED).build())
                        .build(),
                Article.builder()
                        .level(2)
                        .build(),
                Article.builder()
                        .level(3)
                        .video(Video.builder().status(VideoStatus.CERTIFIED).build())
                        .build()));
        int score5 = user.getScore();

        user.submitScore(articles);
        int score6 = user.getScore();

        assertThat(score).isEqualTo(0);
        assertThat(score2).isEqualTo(0);
        assertThat(score3).isEqualTo(0);
        assertThat(score4).isEqualTo(20);
        assertThat(score5).isEqualTo(80);
        assertThat(score6).isEqualTo(
                (int) (19 * 20 + 19 * 20 + 19 * 20 +
                        17 * 10 + 17 * 10 +
                        17 * 5 + 16 * 5 + 16 * 5 +
                        16 * 2.5 + 15 * 2.5));
    }
}
