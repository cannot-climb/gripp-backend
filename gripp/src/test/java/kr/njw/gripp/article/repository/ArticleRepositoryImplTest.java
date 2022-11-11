package kr.njw.gripp.article.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import kr.njw.gripp.article.entity.Article;
import kr.njw.gripp.article.repository.dto.SearchArticleRepoPageToken;
import kr.njw.gripp.article.repository.dto.SearchArticleRepoRequestFilter;
import kr.njw.gripp.article.repository.dto.SearchArticleRepoRequestOrder;
import kr.njw.gripp.video.entity.vo.VideoStatus;
import lombok.Data;
import net.datafaker.Faker;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    @InjectMocks
    private ArticleRepositoryImpl articleRepositoryImpl;
    @Mock
    private JPAQueryFactory jpaQueryFactory;
    @Mock
    private JPAQuery<Article> jpaQuery;
    private Random random;
    private Faker faker;
    private LocalDateTime now;
    private List<Article> sampleArticles;

    @BeforeEach
    void setUp() {
        this.random = new Random(42);
        this.faker = new Faker(this.random);
        this.now = LocalDateTime.now();
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
    void searchRandom() {
        this.setUpJPAQueryFactory();

        final int REQUEST_CASE = 50;
        List<SearchRequest> requests = new ArrayList<>();

        for (int i = 0; i < REQUEST_CASE; i++) {
            SearchRequest request = new SearchRequest();
            int filterCount = this.random.nextInt(20);

            for (int j = 0; j < filterCount; j++) {
                switch (this.random.nextInt(7)) {
                    case 0 -> request.getFilters()
                            .add(SearchArticleRepoRequestFilter.builder()
                                    .username(StringUtils.substring(this.faker.hololive().talent(), 0,
                                            this.random.nextInt(10)))
                                    .build());
                    case 1 -> request.getFilters()
                            .add(SearchArticleRepoRequestFilter.builder()
                                    .titleLike(StringUtils.substring(this.faker.book().title(), 0,
                                            this.random.nextInt(10)))
                                    .build());
                    case 2 -> request.getFilters()
                            .add(SearchArticleRepoRequestFilter.builder()
                                    .minLevel(this.random.nextInt(0, 20))
                                    .maxLevel(this.random.nextInt(0, 20))
                                    .build());
                    case 3 -> request.getFilters()
                            .add(SearchArticleRepoRequestFilter.builder()
                                    .minAngle(this.random.nextInt(0, 90))
                                    .maxAngle(this.random.nextInt(0, 90))
                                    .build());
                    case 4 -> request.getFilters()
                            .add(SearchArticleRepoRequestFilter.builder()
                                    .minDateTime(this.now.minusSeconds(this.random.nextLong(1_000_000_000)))
                                    .maxDateTime(this.now.plusSeconds(this.random.nextLong(1_000_000_000)))
                                    .build());
                    case 5 -> {
                        List<VideoStatus> statusIn = new ArrayList<>();

                        if (this.random.nextInt(2) == 1) {
                            statusIn.add(VideoStatus.PREPROCESSING);
                        }
                        if (this.random.nextInt(2) == 1) {
                            statusIn.add(VideoStatus.NO_CERTIFIED);
                        }
                        if (this.random.nextInt(2) == 1) {
                            statusIn.add(VideoStatus.CERTIFIED);
                        }

                        request.getFilters().add(SearchArticleRepoRequestFilter.builder().statusIn(statusIn).build());
                    }
                    default -> request.getFilters().add(SearchArticleRepoRequestFilter.builder().build());
                }
            }

            switch (this.random.nextInt(6)) {
                case 0 -> request.setOrder(SearchArticleRepoRequestOrder.OLD);
                case 1 -> request.setOrder(SearchArticleRepoRequestOrder.VIEW);
                case 2 -> request.setOrder(SearchArticleRepoRequestOrder.POPULAR);
                case 3 -> request.setOrder(SearchArticleRepoRequestOrder.HARD);
                case 4 -> request.setOrder(SearchArticleRepoRequestOrder.EASY);
                default -> request.setOrder(SearchArticleRepoRequestOrder.NEW);
            }

            if (this.random.nextInt(2) == 0) {
                request.setPageToken(SearchArticleRepoPageToken.EOF);
            } else {
                request.setPageToken(new SearchArticleRepoPageToken(this.random.nextInt(100), this.random.nextInt(100),
                        this.random.nextInt(100), this.random.nextInt(100)));
            }

            request.setLimit(this.random.nextInt(100));
            requests.add(request);
        }

        List<List<Article>> responses = new ArrayList<>();

        for (SearchRequest request : requests) {
            responses.add(this.articleRepositoryImpl.search(request.getFilters(), request.getOrder(),
                    request.getPageToken(), request.getLimit()));
        }

        InOrder inOrder = Mockito.inOrder(this.jpaQuery);

        for (SearchRequest request : requests) {
            then(this.jpaQuery).should(inOrder).innerJoin(article.user, user);
            then(this.jpaQuery).should(inOrder).fetchJoin();
            then(this.jpaQuery).should(inOrder).innerJoin(article.video, video);
            then(this.jpaQuery).should(inOrder).fetchJoin();
            then(this.jpaQuery).should(inOrder)
                    .where(this.getBooleanBuilder(request.getFilters(), request.getOrder(), request.getPageToken()));
            then(this.jpaQuery).should(inOrder).orderBy(this.getOrderSpecifiers(request.getOrder()));
            then(this.jpaQuery).should(inOrder).limit(request.getLimit());
            then(this.jpaQuery).should(inOrder).fetch();
        }

        verifyNoMoreInteractions(this.jpaQuery);

        for (List<Article> articles : responses) {
            assertThat(articles).isEqualTo(this.sampleArticles);
        }
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
        given(this.jpaQuery.where(any(BooleanBuilder.class))).willAnswer(invocation -> {
            BooleanBuilder booleanBuilder = invocation.getArgument(0);
            this.logger.debug(StringUtils.join(booleanBuilder.getValue()));
            return this.jpaQuery;
        });
        given(this.jpaQuery.orderBy(ArgumentMatchers.<OrderSpecifier<?>[]>any())).willReturn(this.jpaQuery);
        given(this.jpaQuery.limit(anyLong())).willReturn(this.jpaQuery);
        given(this.jpaQuery.fetch()).willReturn(this.sampleArticles);
    }

    private BooleanBuilder getBooleanBuilder(List<SearchArticleRepoRequestFilter> filters,
                                             SearchArticleRepoRequestOrder order,
                                             SearchArticleRepoPageToken pageToken) {
        BooleanBuilder booleanBuilder = new BooleanBuilder();

        for (SearchArticleRepoRequestFilter filter : filters) {
            if (filter.getUsername() != null) {
                booleanBuilder.and(article.user.username.eq(filter.getUsername()));
            }

            if (filter.getTitleLike() != null) {
                booleanBuilder.and(article.title.contains(filter.getTitleLike()));
            }

            if (filter.getMinLevel() != null) {
                booleanBuilder.and(article.level.goe(filter.getMinLevel()));
            }

            if (filter.getMaxLevel() != null) {
                booleanBuilder.and(article.level.loe(filter.getMaxLevel()));
            }

            if (filter.getMinAngle() != null) {
                booleanBuilder.and(article.angle.goe(filter.getMinAngle()));
            }

            if (filter.getMaxAngle() != null) {
                booleanBuilder.and(article.angle.loe(filter.getMaxAngle()));
            }

            if (filter.getMinDateTime() != null) {
                booleanBuilder.and(article.registerDateTime.goe(filter.getMinDateTime()));
            }

            if (filter.getMaxDateTime() != null) {
                booleanBuilder.and(article.registerDateTime.loe(filter.getMaxDateTime()));
            }

            if (filter.getStatusIn() != null) {
                booleanBuilder.and(article.video.status.in(filter.getStatusIn()));
            }
        }

        if (pageToken.isValid()) {
            switch (order) {
                case OLD -> booleanBuilder.and(article.id.gt(pageToken.getPrevId()));
                case VIEW -> booleanBuilder.and(new BooleanBuilder()
                        .or(article.viewCount.lt(pageToken.getPrevViewCount()))
                        .or(new BooleanBuilder()
                                .and(article.viewCount.eq(pageToken.getPrevViewCount()))
                                .and(article.id.lt(pageToken.getPrevId()))
                        ));
                case POPULAR -> booleanBuilder.and(new BooleanBuilder()
                        .or(article.favoriteCount.lt(pageToken.getPrevFavoriteCount()))
                        .or(new BooleanBuilder()
                                .and(article.favoriteCount.eq(pageToken.getPrevFavoriteCount()))
                                .and(article.id.lt(pageToken.getPrevId()))
                        ));
                case HARD -> booleanBuilder.and(new BooleanBuilder()
                        .or(article.level.lt(pageToken.getPrevLevel()))
                        .or(new BooleanBuilder()
                                .and(article.level.eq(pageToken.getPrevLevel()))
                                .and(article.id.lt(pageToken.getPrevId()))
                        ));
                case EASY -> booleanBuilder.and(new BooleanBuilder()
                        .or(article.level.gt(pageToken.getPrevLevel()))
                        .or(new BooleanBuilder()
                                .and(article.level.eq(pageToken.getPrevLevel()))
                                .and(article.id.lt(pageToken.getPrevId()))
                        ));
                default -> booleanBuilder.and(article.id.lt(pageToken.getPrevId()));
            }
        }

        return booleanBuilder;
    }

    private OrderSpecifier<?>[] getOrderSpecifiers(SearchArticleRepoRequestOrder order) {
        switch (order) {
            case OLD -> {
                return new OrderSpecifier<?>[]{new OrderSpecifier<>(Order.ASC, article.id)};
            }
            case VIEW -> {
                return new OrderSpecifier<?>[]{new OrderSpecifier<>(Order.DESC, article.viewCount),
                        new OrderSpecifier<>(Order.DESC, article.id)};
            }
            case POPULAR -> {
                return new OrderSpecifier<?>[]{new OrderSpecifier<>(Order.DESC, article.favoriteCount),
                        new OrderSpecifier<>(Order.DESC, article.id)};
            }
            case HARD -> {
                return new OrderSpecifier<?>[]{new OrderSpecifier<>(Order.DESC, article.level),
                        new OrderSpecifier<>(Order.DESC, article.id)};
            }
            case EASY -> {
                return new OrderSpecifier<?>[]{new OrderSpecifier<>(Order.ASC, article.level),
                        new OrderSpecifier<>(Order.DESC, article.id)};
            }
            default -> {
                return new OrderSpecifier<?>[]{new OrderSpecifier<>(Order.DESC, article.id)};
            }
        }
    }

    @Data
    private static class SearchRequest {
        private List<SearchArticleRepoRequestFilter> filters = new ArrayList<>();
        private SearchArticleRepoRequestOrder order;
        private SearchArticleRepoPageToken pageToken;
        private long limit;
    }
}
