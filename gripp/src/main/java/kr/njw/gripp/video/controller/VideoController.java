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
import kr.njw.gripp.video.application.dto.UploadVideoAppResponse;
import kr.njw.gripp.video.controller.dto.UploadVideoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
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

            게시물 등록 전에 단순히 파일만 업로드하는 API임\040\040
            영상 처리 및 게시물 등록은 나중에 이루어짐\040\040
            업로드가 완료되면 영상 아이디를 받고 그 값을 게시물 등록 API에 보내면 됨

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
}
