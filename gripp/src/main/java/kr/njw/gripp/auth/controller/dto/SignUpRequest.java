package kr.njw.gripp.auth.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Data
public class SignUpRequest {
    @Schema(description = "유저 아이디 (영숫자만 사용, 2자 이상 15자 이하)", example = "njw1204")
    @NotNull(message = "must not be null")
    @Size(min = 2, max = 15, message = "size must be between 2 and 15")
    @Pattern(regexp = "\\w*", message = "must contain only alphanumeric characters")
    private String username;

    @Schema(description = "유저 비밀번호 (영숫자 모두 포함, 8자 이상 64자 이하)", example = "pass1234")
    @NotNull(message = "must not be null")
    @Size(min = 8, max = 64, message = "size must be between 8 and 64")
    @Pattern(regexp = "[\\w\\p{Punct}]*", message = "must contain only alphanumeric characters and punctuations")
    @Pattern(regexp = ".*[a-zA-Z].*", message = "must contain an alphabet")
    @Pattern(regexp = ".*[0-9].*", message = "must contain a number")
    private String password;
}
