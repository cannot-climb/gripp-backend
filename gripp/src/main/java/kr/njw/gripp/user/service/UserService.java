package kr.njw.gripp.user.service;

import kr.njw.gripp.user.entity.User;

public interface UserService {
    void noticeNewArticle(User user);

    void noticeNewCertified(User user);
}
