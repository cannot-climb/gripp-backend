package kr.njw.gripp.auth.application.dto;

import lombok.Data;

@Data
public class SignUpAppRequest {
    private String username;
    private String password;
}
