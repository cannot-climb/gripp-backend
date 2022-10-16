package kr.njw.gripp.user.service;

import kr.njw.gripp.article.entity.Article;
import kr.njw.gripp.article.repository.ArticleRepository;
import kr.njw.gripp.user.entity.User;
import kr.njw.gripp.user.repository.UserRepository;
import kr.njw.gripp.video.entity.vo.VideoStatus;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class UserServiceImpl implements UserService {
    private final ArticleRepository articleRepository;
    private final UserRepository userRepository;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Transactional
    public void noticeNewArticle(User user) {
        if (user == null) {
            this.logger.warn("회원이 존재하지 않습니다");
            return;
        }

        user.incrementArticleCount();
        this.userRepository.save(user);
        this.logger.info("새로운 게시물 발생 - " + user.getUsername());
    }

    @Transactional
    public void noticeNewCertified(User user) {
        if (user == null) {
            this.logger.warn("회원이 존재하지 않습니다");
            return;
        }

        user.incrementArticleCertifiedCount();
        this.refreshScore(user);
        this.userRepository.save(user);
        this.logger.info("새로운 등반 성공 발생 - " + user.getUsername());
    }

    @Transactional
    public void noticeDeleteArticle(User user) {
        if (user == null) {
            this.logger.warn("회원이 존재하지 않습니다");
            return;
        }

        user.decrementArticleCount();
        this.userRepository.save(user);
        this.logger.info("게시물 기록 삭제 - " + user.getUsername());
    }

    @Transactional
    public void noticeDeleteCertified(User user) {
        if (user == null) {
            this.logger.warn("회원이 존재하지 않습니다");
            return;
        }

        user.decrementArticleCertifiedCount();
        this.refreshScore(user);
        this.userRepository.save(user);
        this.logger.info("등반 성공 기록 삭제 - " + user.getUsername());
    }

    private void refreshScore(User user) {
        List<Article> articles = this.articleRepository.findTopWithReadLock(user.getId(), VideoStatus.CERTIFIED,
                Pageable.ofSize(User.ARTICLE_MAX_COUNT_FOR_COMPUTE_SCORE));

        user.submitScore(articles);
    }
}
