package kr.njw.gripp.download.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.njw.gripp.download.application.DownloadResourceFactory;
import kr.njw.gripp.global.config.GrippConfig;
import kr.njw.gripp.global.dto.ErrorResponse;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Tag(name = "Download")
@SecurityRequirement(name = "admin")
@RequiredArgsConstructor
@RestController
@RequestMapping("/download")
public class DownloadController {
    private final DownloadResourceFactory downloadResourceFactory;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Operation(summary = "다운로드", description = """
            다운로드 API

            관리자 아이디, 비밀번호 필요

            백엔드 내부적으로 사용하는 API""")
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = @Content(mediaType = "*/*")),
            @ApiResponse(responseCode = "401",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping(value = "/{fileName}")
    public ResponseEntity<?> download(@PathVariable("fileName") String fileName) {
        Path path = Paths.get(GrippConfig.FILE_UPLOAD_PATH, FilenameUtils.getName(fileName));
        Resource resource = this.downloadResourceFactory.createResource(path);
        HttpHeaders headers = new HttpHeaders();

        if (!resource.exists()) {
            this.logger.warn("파일이 없습니다 - " + fileName);
            ErrorResponse errorResponse = new ErrorResponse();
            errorResponse.setErrors(List.of("unknown file"));
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }

        headers.setCacheControl(CacheControl.maxAge(1, TimeUnit.DAYS));
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.set("Content-Disposition",
                "attachment; filename*=UTF-8''" +
                        URLEncoder.encode(fileName, StandardCharsets.UTF_8).replace("+", "%20"));
        return ResponseEntity.ok().headers(headers).body(resource);
    }
}
