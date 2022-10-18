package kr.njw.gripp.article.application.dto.search;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class SearchArticleAppResponse {
    private List<SearchArticleAppResponseItem> articles = new ArrayList<>();
    private String nextPageToken = "";
}
