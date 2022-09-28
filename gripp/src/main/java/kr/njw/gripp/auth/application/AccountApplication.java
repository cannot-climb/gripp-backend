package kr.njw.gripp.auth.application;

import kr.njw.gripp.auth.application.dto.*;

public interface AccountApplication {
    boolean signUp(SignUpAppRequest request);

    LoginAppResponse login(LoginAppRequest request);

    RefreshTokenAppResponse refreshToken(RefreshTokenAppRequest request);

    boolean deleteRefreshToken(DeleteRefreshTokenAppRequest request);

    boolean isUsernameExisted(String username);
}
