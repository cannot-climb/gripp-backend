package kr.njw.gripp.article.application;

import kr.njw.gripp.article.application.dto.*;

public interface ArticleApplication {
    FindArticleAppResponse find(FindArticleAppRequest request);

    WriteArticleAppResponse write(WriteArticleAppRequest request);

    DeleteArticleAppResponse delete(DeleteArticleAppRequest request);

    ReactArticleAppResponse react(ReactArticleAppRequest request);
}
