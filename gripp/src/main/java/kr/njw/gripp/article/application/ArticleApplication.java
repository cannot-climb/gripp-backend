package kr.njw.gripp.article.application;

import kr.njw.gripp.article.application.dto.WriteArticleAppRequest;
import kr.njw.gripp.article.application.dto.WriteArticleAppResponse;

public interface ArticleApplication {
    WriteArticleAppResponse write(WriteArticleAppRequest request);
}
