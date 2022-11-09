package kr.njw.gripp.article.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import kr.njw.gripp.article.entity.Article;
import kr.njw.gripp.article.repository.dto.SearchArticleRepoPageToken;
import kr.njw.gripp.article.repository.dto.SearchArticleRepoRequestFilter;
import kr.njw.gripp.article.repository.dto.SearchArticleRepoRequestOrder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static kr.njw.gripp.article.entity.QArticle.article;
import static kr.njw.gripp.user.entity.QUser.user;
import static kr.njw.gripp.video.entity.QVideo.video;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Repository
public class ArticleRepositoryImpl implements ArticleRepositoryCustom {
    private final JPAQueryFactory jpaQueryFactory;

    public List<Article> search(List<SearchArticleRepoRequestFilter> filters, SearchArticleRepoRequestOrder order,
                                SearchArticleRepoPageToken pageToken, long limit) {
        return this.jpaQueryFactory.selectFrom(article)
                .innerJoin(article.user, user).fetchJoin()
                .innerJoin(article.video, video).fetchJoin()
                .where(this.createBooleanBuilder(filters, order, pageToken))
                .orderBy(this.createOrderSpecifiers(order).toArray(OrderSpecifier<?>[]::new))
                .limit(limit)
                .fetch();
    }

    public SearchArticleRepoPageToken createNextPageToken(List<Article> searchResults) {
        if (searchResults == null || searchResults.isEmpty()) {
            return SearchArticleRepoPageToken.EOF;
        }

        Article lastArticle = searchResults.get(searchResults.size() - 1);
        return new SearchArticleRepoPageToken(lastArticle.getId(), lastArticle.getLevel(),
                lastArticle.getViewCount(), lastArticle.getFavoriteCount());
    }

    private BooleanBuilder createBooleanBuilder(List<SearchArticleRepoRequestFilter> filters,
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

    private List<OrderSpecifier<?>> createOrderSpecifiers(SearchArticleRepoRequestOrder order) {
        switch (order) {
            case OLD -> {
                return List.of(new OrderSpecifier<>(Order.ASC, article.id));
            }
            case VIEW -> {
                return List.of(new OrderSpecifier<>(Order.DESC, article.viewCount),
                        new OrderSpecifier<>(Order.DESC, article.id));
            }
            case POPULAR -> {
                return List.of(new OrderSpecifier<>(Order.DESC, article.favoriteCount),
                        new OrderSpecifier<>(Order.DESC, article.id));
            }
            case HARD -> {
                return List.of(new OrderSpecifier<>(Order.DESC, article.level),
                        new OrderSpecifier<>(Order.DESC, article.id));
            }
            case EASY -> {
                return List.of(new OrderSpecifier<>(Order.ASC, article.level),
                        new OrderSpecifier<>(Order.DESC, article.id));
            }
            default -> {
                return List.of(new OrderSpecifier<>(Order.DESC, article.id));
            }
        }
    }
}
