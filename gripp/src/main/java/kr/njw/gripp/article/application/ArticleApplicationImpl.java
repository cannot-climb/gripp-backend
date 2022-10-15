package kr.njw.gripp.article.application;

import kr.njw.gripp.article.application.dto.WriteArticleAppRequest;
import kr.njw.gripp.article.application.dto.WriteArticleAppResponse;
import kr.njw.gripp.article.application.dto.WriteArticleAppResponseStatus;
import kr.njw.gripp.article.entity.Article;
import kr.njw.gripp.article.repository.ArticleFavoriteRepository;
import kr.njw.gripp.article.repository.ArticleRepository;
import kr.njw.gripp.user.entity.User;
import kr.njw.gripp.user.repository.UserRepository;
import kr.njw.gripp.video.entity.Video;
import kr.njw.gripp.video.entity.vo.VideoStatus;
import kr.njw.gripp.video.repository.VideoRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@Service
public class ArticleApplicationImpl implements ArticleApplication {
    private final ArticleRepository articleRepository;
    private final ArticleFavoriteRepository articleFavoriteRepository;
    private final UserRepository userRepository;
    private final VideoRepository videoRepository;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Transactional
    public WriteArticleAppResponse write(WriteArticleAppRequest request) {
        WriteArticleAppResponse response = new WriteArticleAppResponse();
        Video video = this.videoRepository.findByUuidForUpdate(request.getVideoUuid()).orElse(null);
        User user = this.userRepository.findByUsernameForUpdate(request.getUsername()).orElse(null);

        if (video == null) {
            response.setStatus(WriteArticleAppResponseStatus.NO_VIDEO);
            this.logger.warn("영상이 없습니다 - " + request);
            return response;
        }

        if (user == null) {
            response.setStatus(WriteArticleAppResponseStatus.NO_USER);
            this.logger.warn("유저가 없습니다 - " + request);
            return response;
        }

        if (this.articleRepository.existsByVideoId(video.getId())) {
            response.setStatus(WriteArticleAppResponseStatus.ALREADY_POSTED_VIDEO);
            this.logger.warn("이미 게시된 영상입니다 - " + request);
            return response;
        }

        Article article = Article.builder()
                .user(user)
                .video(video)
                .title(request.getTitle())
                .description(request.getDescription())
                .level(request.getLevel())
                .angle(request.getAngle())
                .registerDateTime(LocalDateTime.now())
                .build();

        this.articleRepository.saveAndFlush(article);
        user.noticeNewArticle(article);

        if (video.getStatus() == VideoStatus.CERTIFIED) {
            // 영상이 CERTIFIED 판정을 먼저 받은 다음에 게시글을 등록한 경우
            user.noticeNewCertified(article);
        }

        this.userRepository.save(user);
        response.setStatus(WriteArticleAppResponseStatus.SUCCESS);
        response.setId(article.getId());

        return response;
    }
}
