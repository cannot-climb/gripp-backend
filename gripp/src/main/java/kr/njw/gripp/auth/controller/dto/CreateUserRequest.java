package kr.njw.gripp.auth.controller.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Data
public class CreateUserRequest {
    @NotNull
    @Size(min = 2, max = 15)
    @Pattern(regexp = "\\w*", message = "must contain only alphanumeric characters")
    private String username;

    @NotNull
    @Size(min = 8, max = 64)
    @Pattern(regexp = "[\\w\\p{Punct}]*", message = "must contain only alphanumeric characters and punctuations")
    @Pattern(regexp = ".*[a-zA-Z].*", message = "must contain an alphabet")
    @Pattern(regexp = ".*[0-9].*", message = "must contain a number")
    private String password;
}
