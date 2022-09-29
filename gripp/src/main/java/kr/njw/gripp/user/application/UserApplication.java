package kr.njw.gripp.user.application;

import kr.njw.gripp.user.application.dto.FindUserAppResponse;

public interface UserApplication {
    FindUserAppResponse findUser(String username);
}
