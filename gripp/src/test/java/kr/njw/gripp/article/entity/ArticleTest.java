package kr.njw.gripp.article.entity;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ArticleTest {
    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void edit() {
        Article article = Article.builder().title("a").description("b").build();

        article.edit("테스트 제목", "내용 테스트");

        assertThat(article.getTitle()).isEqualTo("테스트 제목");
        assertThat(article.getDescription()).isEqualTo("내용 테스트");
    }
}
