package kr.njw.gripp.article.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.njw.gripp.article.application.ArticleApplication;
import kr.njw.gripp.article.application.dto.WriteArticleAppRequest;
import kr.njw.gripp.article.application.dto.WriteArticleAppResponse;
import kr.njw.gripp.article.controller.dto.WriteArticleRequest;
import kr.njw.gripp.article.controller.dto.WriteArticleResponse;
import kr.njw.gripp.global.dto.ErrorResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.security.Principal;
import java.util.List;

@Tag(name = "Article")
@SecurityRequirement(name = "accessToken")
@RequiredArgsConstructor
@RestController
@RequestMapping("/articles")
public class ArticleController {
    private final ArticleApplication articleApplication;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Operation(summary = "게시물 등록", description = """
            게시물 등록 API

            먼저 영상 업로드 API 호출 후 게시물 등록 API를 호출해야 됨\040\040
            영상 인코딩이 끝나기 전이라도 바로 게시물 등록 API를 호출하면 됨

            게시물 등록이 완료되면 즉시 게시물 검색 API 등에서 게시물은 조회 가능\040\040
            하지만 영상 인코딩 완료 전까지 영상의 스트리밍 주소, 등반 성공 여부 등은 제공되지 않음""")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "등록 완료",
                    content = @Content(schema = @Schema(implementation = WriteArticleResponse.class))),
            @ApiResponse(responseCode = "400", description = "등록 실패 (Bad Request) / ex: 입력 제약 조건에 맞지 않는 경우",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "등록 실패 (Unauthorized)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "등록 실패 (Conflict) / ex: 이미 게시된 영상을 다시 게시하려는 경우",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    })
    @PostMapping("")
    public ResponseEntity<?> writeArticle(@Valid @RequestBody WriteArticleRequest request, Principal principal) {
        WriteArticleAppRequest appRequest = new WriteArticleAppRequest();
        appRequest.setUsername(principal.getName());
        appRequest.setVideoUuid(request.getVideoId());
        appRequest.setTitle(request.getTitle());
        appRequest.setDescription(request.getDescription());
        appRequest.setLevel(request.getLevel());
        appRequest.setAngle(request.getAngle());

        WriteArticleAppResponse appResponse = this.articleApplication.write(appRequest);

        if (appResponse.getId().isEmpty()) {
            ErrorResponse errorResponse = new ErrorResponse();
            errorResponse.setErrors(List.of(switch (appResponse.getStatus()) {
                case NO_USER -> "invalid user";
                case NO_VIDEO -> "invalid video";
                case ALREADY_POSTED_VIDEO -> "already posted video";
                default -> "fail to write";
            }));
            this.logger.warn("게시물 등록 실패 - " + request + ", " + errorResponse);
            return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
        }

        WriteArticleResponse response = new WriteArticleResponse();
        response.setArticleId(appResponse.getId().get().toString());
        return ResponseEntity.ok(response);
    }
}
