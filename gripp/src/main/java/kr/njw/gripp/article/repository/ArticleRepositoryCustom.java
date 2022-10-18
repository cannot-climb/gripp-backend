package kr.njw.gripp.article.repository;

import kr.njw.gripp.article.entity.Article;
import kr.njw.gripp.article.repository.dto.SearchArticleRepoPageToken;
import kr.njw.gripp.article.repository.dto.SearchArticleRepoRequestFilter;
import kr.njw.gripp.article.repository.dto.SearchArticleRepoRequestOrder;

import java.util.List;

public interface ArticleRepositoryCustom {
    List<Article> search(List<SearchArticleRepoRequestFilter> filters, SearchArticleRepoRequestOrder order,
                         SearchArticleRepoPageToken pageToken, long limit);

    SearchArticleRepoPageToken createNextPageToken(List<Article> searchResults);
}
