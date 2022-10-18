package kr.njw.gripp.article.controller.dto.search;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.Valid;
import java.util.List;

@Data
public class SearchArticleRequest {
    @Valid
    @ArraySchema(
            arraySchema = @Schema(description = "검색 필터 (여러 필터 포함시 AND 조건으로 검색, 필터가 없으면 모든 게시물 검색)"),
            schema = @Schema(oneOf = {SearchArticleRequestTitleFilter.class, SearchArticleRequestUserFilter.class,
                    SearchArticleRequestLevelFilter.class, SearchArticleRequestAngleFilter.class,
                    SearchArticleRequestDateTimeFilter.class, SearchArticleRequestStatusFilter.class}))
    private List<SearchArticleRequestFilter> filters;
    @Schema(description = "정렬 순서 (NEW: 최신순, OLD: 과거순, VIEW: 조회순, POPULAR: 좋아요순, HARD: 어려운순, EASY: 쉬운순, 필드를 생략하면 최신순)",
            example = "NEW")
    private SearchArticleRequestOrder order;
    @Schema(description = "페이징용 토큰, 값을 보내면 해당 페이지부터 검색 (필드를 생략하면 처음부터 검색)",
            example = "NbVLWuyLVpF6zeu4fX-m7aO66lMeoim_01v9LdB")
    private String pageToken;
}
