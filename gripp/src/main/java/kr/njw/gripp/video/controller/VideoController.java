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

    @Operation(summary = "영상 업로드", description = "영상 업로드 API")
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
