package kr.njw.gripp.article.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import kr.njw.gripp.article.entity.Article;
import kr.njw.gripp.article.repository.dto.SearchArticleRepoPageToken;
import kr.njw.gripp.article.repository.dto.SearchArticleRepoRequestOrder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static kr.njw.gripp.article.entity.QArticle.article;
import static kr.njw.gripp.user.entity.QUser.user;
import static kr.njw.gripp.video.entity.QVideo.video;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class ArticleRepositoryImplTest {
    @InjectMocks
    private ArticleRepositoryImpl articleRepositoryImpl;
    @Mock
    private JPAQueryFactory jpaQueryFactory;
    @Mock
    private JPAQuery<Article> jpaQuery;
    private List<Article> sampleArticles;

    @BeforeEach
    void setUp() {
        this.sampleArticles = List.of(Article.builder().id(5822L).build(), Article.builder().id(235L).build());
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void searchDefault() {
        this.setUpJPAQueryFactory();

        List<Article> articles =
                this.articleRepositoryImpl.search(new ArrayList<>(), SearchArticleRepoRequestOrder.NEW,
                        SearchArticleRepoPageToken.EOF, Integer.MAX_VALUE);

        InOrder inOrder = Mockito.inOrder(this.jpaQuery);

        then(this.jpaQuery).should(inOrder).innerJoin(article.user, user);
        then(this.jpaQuery).should(inOrder).fetchJoin();
        then(this.jpaQuery).should(inOrder).innerJoin(article.video, video);
        then(this.jpaQuery).should(inOrder).fetchJoin();
        then(this.jpaQuery).should(inOrder)
                .where(ArgumentMatchers.<BooleanBuilder>argThat(argument -> argument.getValue() == null));
        then(this.jpaQuery).should(inOrder)
                .orderBy(new OrderSpecifier<?>[]{new OrderSpecifier<>(Order.DESC, article.id)});
        then(this.jpaQuery).should(inOrder).limit(Integer.MAX_VALUE);
        then(this.jpaQuery).should(inOrder).fetch();
        verifyNoMoreInteractions(this.jpaQuery);

        assertThat(articles).isEqualTo(this.sampleArticles);
    }

    @Test
    void createNextPageToken() {
        SearchArticleRepoPageToken pageTokenNull = this.articleRepositoryImpl.createNextPageToken(null);
        SearchArticleRepoPageToken pageTokenEmpty = this.articleRepositoryImpl.createNextPageToken(new ArrayList<>());
        SearchArticleRepoPageToken pageToken = this.articleRepositoryImpl.createNextPageToken(
                List.of(Article.builder().id(1L).level(2).viewCount(3).favoriteCount(4).build(),
                        Article.builder().id(2L).level(3).viewCount(4).favoriteCount(5).build()));

        assertThat(pageTokenNull).isSameAs(SearchArticleRepoPageToken.EOF);
        assertThat(pageTokenEmpty).isSameAs(SearchArticleRepoPageToken.EOF);
        assertThat(pageToken.isValid()).isTrue();
        assertThat(pageToken.getPrevId()).isEqualTo(2L);
        assertThat(pageToken.getPrevLevel()).isEqualTo(3);
        assertThat(pageToken.getPrevViewCount()).isEqualTo(4);
        assertThat(pageToken.getPrevFavoriteCount()).isEqualTo(5);
    }

    private void setUpJPAQueryFactory() {
        given(this.jpaQueryFactory.selectFrom(article)).willReturn(this.jpaQuery);
        given(this.jpaQuery.innerJoin(article.user, user)).willReturn(this.jpaQuery);
        given(this.jpaQuery.innerJoin(article.video, video)).willReturn(this.jpaQuery);
        given(this.jpaQuery.fetchJoin()).willReturn(this.jpaQuery);
        given(this.jpaQuery.where(any(BooleanBuilder.class))).willReturn(this.jpaQuery);
        given(this.jpaQuery.orderBy(ArgumentMatchers.<OrderSpecifier<?>[]>any())).willReturn(this.jpaQuery);
        given(this.jpaQuery.limit(anyLong())).willReturn(this.jpaQuery);
        given(this.jpaQuery.fetch()).willReturn(this.sampleArticles);
    }
}
