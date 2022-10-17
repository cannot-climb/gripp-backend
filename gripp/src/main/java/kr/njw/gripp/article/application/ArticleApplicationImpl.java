package kr.njw.gripp.article.application;

import kr.njw.gripp.article.application.dto.*;
import kr.njw.gripp.article.entity.Article;
import kr.njw.gripp.article.entity.ArticleFavorite;
import kr.njw.gripp.article.repository.ArticleFavoriteRepository;
import kr.njw.gripp.article.repository.ArticleRepository;
import kr.njw.gripp.user.application.UserApplication;
import kr.njw.gripp.user.application.dto.FindUserAppResponse;
import kr.njw.gripp.user.entity.User;
import kr.njw.gripp.user.repository.UserRepository;
import kr.njw.gripp.user.service.UserService;
import kr.njw.gripp.video.application.VideoApplication;
import kr.njw.gripp.video.application.dto.FindVideoAppResponse;
import kr.njw.gripp.video.entity.Video;
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
    private final UserApplication userApplication;
    private final VideoApplication videoApplication;
    private final UserService userService;
    private final ArticleRepository articleRepository;
    private final ArticleFavoriteRepository articleFavoriteRepository;
    private final UserRepository userRepository;
    private final VideoRepository videoRepository;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Transactional
    public FindArticleAppResponse find(FindArticleAppRequest request) {
        FindArticleAppResponse response = new FindArticleAppResponse();
        Article article = this.articleRepository.findById(request.getArticleId()).orElse(null);
        User requester = this.userRepository.findByUsername(request.getUsernameRequestedBy()).orElse(null);

        if (article == null) {
            response.setStatus(FindArticleAppResponseStatus.NO_ARTICLE);
            this.logger.warn("게시물이 없습니다 - " + request);
            return response;
        }

        FindUserAppResponse findUserAppResponse = this.userApplication.findUser(article.getUser().getUsername());
        FindVideoAppResponse findVideoAppResponse = this.videoApplication.findVideo(article.getVideo().getUuid());

        if (!findUserAppResponse.isSuccess()) {
            response.setStatus(FindArticleAppResponseStatus.NO_ARTICLE);
            this.logger.error("게시물에 매칭된 유저가 없습니다 - " + findUserAppResponse);
            return response;
        }

        if (!findVideoAppResponse.isSuccess()) {
            response.setStatus(FindArticleAppResponseStatus.NO_ARTICLE);
            this.logger.error("게시물에 매칭된 영상이 없습니다 - " + findVideoAppResponse);
            return response;
        }

        if (requester != null && !requester.getUsername().equals(findUserAppResponse.getUsername().orElse(null))) {
            this.articleRepository.incrementViewCountById(article.getId());
            article = this.articleRepository.findById(request.getArticleId()).orElseThrow();
            this.logger.info("게시물 조회수 증가 - " + request);
        }

        response.setStatus(FindArticleAppResponseStatus.SUCCESS);
        response.setId(article.getId());
        response.setUser(findUserAppResponse);
        response.setVideo(findVideoAppResponse);
        response.setTitle(article.getTitle());
        response.setDescription(article.getDescription());
        response.setLevel(article.getLevel());
        response.setAngle(article.getAngle());
        response.setViewCount(article.getViewCount());
        response.setFavoriteCount(article.getFavoriteCount());
        response.setRegisterDateTime(article.getRegisterDateTime());

        if (requester != null) {
            response.setFavorite(this.articleFavoriteRepository.existsByArticleIdAndUserId(
                    article.getId(), requester.getId()));
        } else {
            response.setFavorite(false);
        }

        return response;
    }

    @Transactional
    public WriteArticleAppResponse write(WriteArticleAppRequest request) {
        WriteArticleAppResponse response = new WriteArticleAppResponse();
        Video video = this.videoRepository.findForShareByUuid(request.getVideoUuid()).orElse(null);
        User user = this.userRepository.findForUpdateByUsername(request.getUsername()).orElse(null);

        if (video == null) {
            response.setStatus(WriteArticleAppResponseStatus.NO_VIDEO);
            this.logger.warn("영상이 없습니다 - " + request);
            return response;
        }

        if (user == null) {
            response.setStatus(WriteArticleAppResponseStatus.NO_USER);
            this.logger.error("유저가 없습니다 - " + request);
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
        this.userService.noticeNewArticle(user);

        if (video.isCertified()) {
            // 영상이 CERTIFIED 판정을 먼저 받은 다음에 게시글을 등록한 경우
            this.userService.noticeNewCertified(user);
        }

        response.setStatus(WriteArticleAppResponseStatus.SUCCESS);
        response.setId(article.getId());
        return response;
    }

    @Transactional
    public DeleteArticleAppResponse delete(DeleteArticleAppRequest request) {
        DeleteArticleAppResponse response = new DeleteArticleAppResponse();
        Article article = this.articleRepository.findForUpdateById(request.getArticleId()).orElse(null);

        if (article == null) {
            response.setStatus(DeleteArticleAppResponseStatus.NO_ARTICLE);
            this.logger.warn("게시물이 없습니다 - " + request);
            return response;
        }

        Video video = article.getVideo();
        User user = article.getUser();

        if (user == null || !user.getUsername().equals(request.getUsername())) {
            response.setStatus(DeleteArticleAppResponseStatus.FORBIDDEN);
            this.logger.warn("잘못된 유저입니다 - " + request + ", " + user);
            return response;
        }

        this.articleRepository.delete(article);
        this.videoRepository.delete(video);
        this.userService.noticeDeleteArticle(user);

        if (video.isCertified()) {
            this.userService.noticeDeleteCertified(user);
        }

        response.setStatus(DeleteArticleAppResponseStatus.SUCCESS);
        return response;
    }

    @Transactional
    public ReactArticleAppResponse react(ReactArticleAppRequest request) {
        ReactArticleAppResponse response = new ReactArticleAppResponse();
        Article article = this.articleRepository.findById(request.getArticleId()).orElse(null);
        User user = this.userRepository.findByUsername(request.getUsernameRequestedBy()).orElse(null);

        if (article == null) {
            response.setStatus(ReactArticleAppResponseStatus.NO_ARTICLE);
            this.logger.warn("게시물이 없습니다 - " + request);
            return response;
        }

        if (user == null) {
            response.setStatus(ReactArticleAppResponseStatus.SUCCESS);
            this.logger.error("유저가 없습니다 - " + request);
            return response;
        }

        if (request.isFavorite()) {
            if (!this.articleFavoriteRepository.existsByArticleIdAndUserId(article.getId(), user.getId())) {
                ArticleFavorite articleFavorite = ArticleFavorite.builder()
                        .article(article)
                        .user(user)
                        .registerDateTime(LocalDateTime.now())
                        .build();

                this.articleFavoriteRepository.save(articleFavorite);
                this.articleRepository.incrementFavoriteCountById(article.getId());
                this.logger.info("게시물 리액션 발생 - " + request);
            }
        } else {
            this.articleFavoriteRepository.findByArticleIdAndUserId(article.getId(), user.getId())
                    .ifPresent(articleFavorite -> {
                        this.articleFavoriteRepository.delete(articleFavorite);
                        this.articleRepository.decrementFavoriteCountById(article.getId());
                        this.logger.info("게시물 리액션 발생 - " + request);
                    });
        }

        response.setStatus(ReactArticleAppResponseStatus.SUCCESS);
        response.setFavorite(request.isFavorite());
        return response;
    }
}
