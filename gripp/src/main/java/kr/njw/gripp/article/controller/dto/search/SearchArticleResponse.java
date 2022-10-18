package kr.njw.gripp.article.controller.dto.search;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class SearchArticleResponse {
    @ArraySchema(arraySchema = @Schema(description = "게시글 목록 (정렬 및 페이징 처리됨, 최대 30개)"), maxItems = 30)
    private List<SearchArticleResponseItem> articles;
    @Schema(description = """
            페이징용 토큰, 해당 토큰으로 검색 API를 호출하면 다음 페이지부터 검색 가능\040\040
            다만, 다음 페이지를 검색했을 때 검색 결과가 없을 수 있음 (실제로 검색하기 전에는 검색 결과가 더 있는지 알 수 없음)

            현재 검색 결과가 없는 경우에, 즉 articles 배열이 비어있는 경우에 nextPageToken은 공백 문자열임 (검색 결과의 끝)""",
            example = "NbVLWuyLVpF6zeu4fX-m7aO66lMeoim_01v9LdB")
    private String nextPageToken;
}
