package kr.njw.gripp.article.application;

import kr.njw.gripp.article.application.dto.*;
import kr.njw.gripp.article.application.dto.search.SearchArticleAppRequest;
import kr.njw.gripp.article.application.dto.search.SearchArticleAppResponse;

public interface ArticleApplication {
    FindArticleAppResponse find(FindArticleAppRequest request);

    WriteArticleAppResponse write(WriteArticleAppRequest request);

    DeleteArticleAppResponse delete(DeleteArticleAppRequest request);

    ReactArticleAppResponse react(ReactArticleAppRequest request);

    SearchArticleAppResponse search(SearchArticleAppRequest request);
}
