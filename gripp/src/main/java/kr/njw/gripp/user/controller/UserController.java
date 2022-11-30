package kr.njw.gripp.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.njw.gripp.global.dto.ErrorResponse;
import kr.njw.gripp.user.application.UserApplication;
import kr.njw.gripp.user.application.dto.FindLeaderBoardAppResponse;
import kr.njw.gripp.user.application.dto.FindUserAppResponse;
import kr.njw.gripp.user.controller.dto.FindLeaderBoardResponse;
import kr.njw.gripp.user.controller.dto.FindUserResponse;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

@Tag(name = "User")
@SecurityRequirement(name = "accessToken")
@RequiredArgsConstructor
@RestController
@RequestMapping("/users")
public class UserController {
    private final static String[] EMOJIS = StringUtils.split("""
            😀 😃 😄 😁 😆\s
            😅 😂 🤣 😊 😇 🙂 🙃\s
            😉 😌 😍 🥰 😘 😗 😙 😚\s
            😋 😛 😝 😜 🤪 🤨 🧐 🤓\s
            😎 🤩 🥳 😏 😒 😞 😔 😟\s
            😕 🙁 😣 😖 😫 😩 🥺\s
            😢 😭 😤 😠 😡 🤬 🤯 😳\s
            🥵 🥶 😱 😨 😰 😥 😓 🤗\s
            🤔 🤭 🤫 🤥 😶 😐 😑 😬\s
            🙄 😯 😦 😧 😮 😲 🥱 😴\s
            🤤 😪 😵 🤐 🥴 🤢 🤮 🤧\s
            😷 🤒 🤕 🤑 🤠 😈 👿 👹\s
            👺 🤡 👻 💀 👽 👾\s
            🤖 🎃 😺 😸 😹 😻 😼 😽\s
            🙀 😿 😾""");

    private final UserApplication userApplication;

    public static FindUserResponse createFindUserResponse(FindUserAppResponse appResponse) {
        FindUserResponse response = new FindUserResponse();
        response.setUsername(appResponse.getUsername().orElseThrow());
        response.setTier(appResponse.getTier());
        response.setScore(appResponse.getScore() / 100.0f);
        response.setRank(appResponse.getRank());
        response.setPercentile(appResponse.getPercentile());
        response.setArticleCount(appResponse.getArticleCount());
        response.setArticleCertifiedCount(appResponse.getArticleCertifiedCount());
        response.setRegisterDateTime(appResponse.getRegisterDateTime().orElseThrow());
        return response;
    }

    @Operation(summary = "회원정보", description = """
            회원정보 API

            URI에 주어진 회원의 정보를 반환""")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 완료",
                    content = @Content(schema = @Schema(implementation = FindUserResponse.class))),
            @ApiResponse(responseCode = "400", description = "조회 실패 (Bad Request)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "조회 실패 (Unauthorized)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "조회 실패 (Not Found) / ex: 아이디가 존재하지 않는 경우",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    })
    @GetMapping("/{username}")
    public ResponseEntity<?> findUser(
            @Parameter(description = "유저 아이디", example = "njw1204") @PathVariable("username") String username) {
        FindUserAppResponse appResponse = this.userApplication.findUser(username);

        if (!appResponse.isSuccess()) {
            ErrorResponse errorResponse = new ErrorResponse();
            errorResponse.setErrors(List.of("fail to find user"));
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }

        return ResponseEntity.ok(createFindUserResponse(appResponse));
    }

    @Operation(summary = "리더보드", description = """
            리더보드 API

            URI에 주어진 회원 기준의 리더보드를 반환""")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 완료",
                    content = @Content(schema = @Schema(implementation = FindLeaderBoardResponse.class))),
            @ApiResponse(responseCode = "400", description = "조회 실패 (Bad Request)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "조회 실패 (Unauthorized)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "조회 실패 (Not Found) / ex: 아이디가 존재하지 않는 경우",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    })
    @GetMapping("/{username}/leaderboard")
    public ResponseEntity<?> findLeaderBoard(
            @Parameter(description = "유저 아이디", example = "njw1204") @PathVariable("username") String username) {
        FindLeaderBoardAppResponse appResponse = this.userApplication.findLeaderBoard(username);

        if (!appResponse.isSuccess()) {
            ErrorResponse errorResponse = new ErrorResponse();
            errorResponse.setErrors(List.of("fail to find leaderboard"));
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }

        appResponse.getDefaultBoard().forEach(defaultBoardItem -> {
            long seed = UUID.nameUUIDFromBytes(defaultBoardItem.getUsername().orElseThrow().getBytes())
                    .getMostSignificantBits();
            defaultBoardItem.setUsername(EMOJIS[(new Random(seed)).nextInt(EMOJIS.length)] + " " +
                    defaultBoardItem.getUsername().orElseThrow());
        });

        FindLeaderBoardResponse response = new FindLeaderBoardResponse();
        response.setTopBoard(appResponse.getTopBoard().stream()
                .map(UserController::createFindUserResponse).collect(Collectors.toList()));
        response.setDefaultBoard(appResponse.getDefaultBoard().stream()
                .map(UserController::createFindUserResponse).collect(Collectors.toList()));

        return ResponseEntity.ok(response);
    }
}
