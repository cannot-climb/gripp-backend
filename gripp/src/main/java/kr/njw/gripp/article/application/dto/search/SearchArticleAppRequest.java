package kr.njw.gripp.article.application.dto.search;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class SearchArticleAppRequest {
    private List<SearchArticleAppRequestFilter> filters = new ArrayList<>();
    private SearchArticleAppRequestOrder order;
    private String pageToken;
}
