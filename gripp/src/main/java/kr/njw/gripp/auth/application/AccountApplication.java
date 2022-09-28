package kr.njw.gripp.auth.application;

import kr.njw.gripp.auth.application.dto.LoginAppRequest;
import kr.njw.gripp.auth.application.dto.LoginAppResponse;
import kr.njw.gripp.auth.application.dto.SignUpAppRequest;

public interface AccountApplication {
    boolean signUp(SignUpAppRequest request);

    LoginAppResponse login(LoginAppRequest request);

    boolean isUsernameExisted(String username);
}
