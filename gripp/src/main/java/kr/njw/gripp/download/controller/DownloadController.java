package kr.njw.gripp.download.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.njw.gripp.global.config.GrippConfig;
import kr.njw.gripp.global.dto.ErrorResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Tag(name = "Download")
@SecurityRequirement(name = "admin")
@RequiredArgsConstructor
@RestController
@RequestMapping("/download")
public class DownloadController {
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
        try {
            Path path = Paths.get(GrippConfig.FILE_UPLOAD_PATH, fileName);
            Resource resource = new InputStreamResource(Files.newInputStream(path));
            HttpHeaders headers = new HttpHeaders();

            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.set("Content-Disposition",
                    "attachment; filename*=UTF-8''" +
                            URLEncoder.encode(fileName, StandardCharsets.UTF_8).replace("+", "%20"));
            headers.set("Cache-Control", "max-age=86400");
            return ResponseEntity.ok().headers(headers).body(resource);
        } catch (IOException e) {
            this.logger.warn("파일이 없습니다 - " + e);
            ErrorResponse errorResponse = new ErrorResponse();
            errorResponse.setErrors(List.of("unknown file"));
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }
}
