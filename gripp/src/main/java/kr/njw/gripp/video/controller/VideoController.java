package kr.njw.gripp.video.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.njw.gripp.global.dto.ErrorResponse;
import kr.njw.gripp.video.application.VideoApplication;
import kr.njw.gripp.video.application.dto.FindVideoAppResponse;
import kr.njw.gripp.video.application.dto.UploadVideoAppResponse;
import kr.njw.gripp.video.controller.dto.FindVideoResponse;
import kr.njw.gripp.video.controller.dto.UploadVideoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Tag(name = "Video")
@SecurityRequirement(name = "accessToken")
@RequiredArgsConstructor
@RestController
@RequestMapping("/videos")
public class VideoController {
    private final VideoApplication videoApplication;

    @Operation(summary = "영상 업로드", description = """
            영상 업로드 API

            multipart/form-data 형식으로 업로드

            게시물 등록 전에 파일을 업로드하고 영상 인코딩을 시작하는 API임\040\040
            영상 업로드 API 호출 후 추가로 게시물 등록 API를 호출해야 됨\040\040

            파일 업로드가 끝나도 완료 시점에 엑세스 토큰이 만료된 상태이면 HTTP 401 에러가 반환됨\040\040
            따라서 업로드가 길어지면 업로드 중간에 토큰이 만료되어 HTTP 401 에러가 반환될 수 있음\040\040
            업로드 중 토큰 만료 방지를 위해, 반드시 토큰 갱신 API를 먼저 한 번 호출하고 업로드 진행 바람

            파일 업로드는 다소 시간이 걸림 (유선 네트워크에서 100MB 업로드에 빨라야 10초)\040\040
            실패하는 경우도 많을 수 있으니 예외 처리 바람""")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "업로드 완료",
                    content = @Content(schema = @Schema(implementation = UploadVideoResponse.class))),
            @ApiResponse(responseCode = "400", description = "업로드 실패 (Bad Request)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "업로드 실패 (Unauthorized)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping(value = "", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadVideo(
            @Parameter(description = "영상 파일 (mp4, mov만 허용. 영상 최소 길이 5초. 파일 최대 크기 1GB)", required = true)
            @RequestPart("file")
            MultipartFile file)
            throws IOException {
        UploadVideoAppResponse appResponse = this.videoApplication.uploadVideo(file);

        if (!appResponse.isSuccess()) {
            ErrorResponse errorResponse = new ErrorResponse();
            errorResponse.setErrors(
                    List.of(appResponse.getMessage().isBlank() ? "unknown error" : appResponse.getMessage()));
            return ResponseEntity.badRequest().body(errorResponse);
        }

        UploadVideoResponse response = new UploadVideoResponse();
        response.setVideoId(appResponse.getUuid());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "영상 정보", description = """
            영상 정보 API

            현재 앱에서는 사용할 필요가 없지만 테스트 편의를 위해 제공""")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 완료",
                    content = @Content(schema = @Schema(implementation = FindVideoResponse.class))),
            @ApiResponse(responseCode = "400", description = "조회 실패 (Bad Request)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "조회 실패 (Unauthorized)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "조회 실패 (Not Found) / ex: 영상이 존재하지 않는 경우",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    })
    @GetMapping("/{videoId}")
    public ResponseEntity<?> findVideo(
            @Parameter(description = "영상 아이디", example = "7dc53df5-703e-49b3-8670-b1c468f47f1f")
            @PathVariable("videoId") String videoId) {
        FindVideoAppResponse appResponse = this.videoApplication.findVideo(videoId);

        if (!appResponse.isSuccess()) {
            ErrorResponse errorResponse = new ErrorResponse();
            errorResponse.setErrors(List.of("fail to find video"));
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }

        FindVideoResponse response = new FindVideoResponse();
        response.setVideoId(appResponse.getUuid());
        response.setStreamingUrl(appResponse.getStreamingUrl());
        response.setStreamingLength(appResponse.getStreamingLength());
        response.setStreamingAspectRatio(appResponse.getStreamingAspectRatio());
        response.setThumbnailUrl(appResponse.getThumbnailUrl());
        response.setStatus(appResponse.getStatus());
        return ResponseEntity.ok(response);
    }
}
