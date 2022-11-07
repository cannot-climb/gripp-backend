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
import kr.njw.gripp.video.entity.vo.VideoStatus;
import kr.njw.gripp.video.repository.VideoRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class ArticleApplicationImplTest {
    @InjectMocks
    private ArticleApplicationImpl articleApplicationImpl;
    @Mock
    private UserApplication userApplication;
    @Mock
    private VideoApplication videoApplication;
    @Mock
    private UserService userService;
    @Mock
    private ArticleRepository articleRepository;
    @Mock
    private ArticleFavoriteRepository articleFavoriteRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private VideoRepository videoRepository;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        this.now = LocalDateTime.now();
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void find() {
        User author = User.builder()
                .id(10L)
                .username("test")
                .build();

        User requester = User.builder()
                .id(20L)
                .username("test2")
                .build();

        Video video = Video.builder()
                .id(100L)
                .uuid("unique")
                .build();

        Article article = Article.builder()
                .id(1000L)
                .user(author)
                .video(video)
                .title("my video")
                .description("desc")
                .level(19)
                .angle(23)
                .viewCount(3282)
                .favoriteCount(672)
                .registerDateTime(this.now)
                .build();

        Article articleInvalidUser = Article.builder()
                .id(1001L)
                .user(User.builder().username("no").build())
                .video(video)
                .build();

        Article articleInvalidVid = Article.builder()
                .id(1002L)
                .user(author)
                .video(Video.builder().uuid("no").build())
                .build();

        FindUserAppResponse userAppResponse = new FindUserAppResponse();
        userAppResponse.setSuccess(true);
        userAppResponse.setUsername(author.getUsername());
        FindUserAppResponse userAppResponseFail = new FindUserAppResponse();
        userAppResponseFail.setSuccess(false);

        FindVideoAppResponse videoAppResponse = new FindVideoAppResponse();
        videoAppResponse.setSuccess(true);
        videoAppResponse.setUuid(video.getUuid());
        FindVideoAppResponse videoAppResponseFail = new FindVideoAppResponse();
        videoAppResponseFail.setSuccess(false);

        FindArticleAppRequest requestNoArticle = new FindArticleAppRequest();
        requestNoArticle.setArticleId(1L);
        requestNoArticle.setUsernameRequestedBy("guest");

        FindArticleAppRequest requestInvalidAuthor = new FindArticleAppRequest();
        requestInvalidAuthor.setArticleId(articleInvalidUser.getId());
        requestInvalidAuthor.setUsernameRequestedBy("guest");

        FindArticleAppRequest requestInvalidVideo = new FindArticleAppRequest();
        requestInvalidVideo.setArticleId(articleInvalidVid.getId());
        requestInvalidVideo.setUsernameRequestedBy("guest");

        FindArticleAppRequest requestByGuest = new FindArticleAppRequest();
        requestByGuest.setArticleId(article.getId());
        requestByGuest.setUsernameRequestedBy("guest");

        FindArticleAppRequest requestByAuthor = new FindArticleAppRequest();
        requestByAuthor.setArticleId(article.getId());
        requestByAuthor.setUsernameRequestedBy(author.getUsername());

        FindArticleAppRequest requestByRequester = new FindArticleAppRequest();
        requestByRequester.setArticleId(article.getId());
        requestByRequester.setUsernameRequestedBy(requester.getUsername());

        given(this.articleRepository.findById(any())).willReturn(Optional.empty());
        given(this.articleRepository.findById(article.getId())).willReturn(Optional.of(article));
        given(this.articleRepository.findById(articleInvalidUser.getId())).willReturn(Optional.of(articleInvalidUser));
        given(this.articleRepository.findById(articleInvalidVid.getId())).willReturn(Optional.of(articleInvalidVid));
        willAnswer(invocation -> {
            ReflectionTestUtils.setField(article, "viewCount", article.getViewCount() + 1);
            return null;
        }).given(this.articleRepository).incrementViewCountById(article.getId());

        given(this.articleFavoriteRepository.existsByArticleIdAndUserId(any(), any())).willReturn(false);
        given(this.articleFavoriteRepository.existsByArticleIdAndUserId(article.getId(), requester.getId()))
                .willReturn(true);

        given(this.userRepository.findByUsername(any())).willReturn(Optional.empty());
        given(this.userRepository.findByUsername(author.getUsername())).willReturn(Optional.of(author));
        given(this.userRepository.findByUsername(requester.getUsername())).willReturn(Optional.of(requester));

        given(this.userApplication.findUser(any())).willReturn(userAppResponseFail);
        given(this.userApplication.findUser(author.getUsername())).willReturn(userAppResponse);

        given(this.videoApplication.findVideo(any())).willReturn(videoAppResponseFail);
        given(this.videoApplication.findVideo(video.getUuid())).willReturn(videoAppResponse);

        FindArticleAppResponse responseNoArticle = this.articleApplicationImpl.find(requestNoArticle);
        FindArticleAppResponse responseInvalidAuthor = this.articleApplicationImpl.find(requestInvalidAuthor);
        FindArticleAppResponse responseInvalidVideo = this.articleApplicationImpl.find(requestInvalidVideo);
        FindArticleAppResponse responseByGuest = this.articleApplicationImpl.find(requestByGuest);
        FindArticleAppResponse responseByAuthor = this.articleApplicationImpl.find(requestByAuthor);
        FindArticleAppResponse responseByRequester = this.articleApplicationImpl.find(requestByRequester);

        then(this.articleRepository).should(times(1)).incrementViewCountById(any());
        then(this.articleRepository).should(times(1)).incrementViewCountById(article.getId());

        assertThat(responseNoArticle.getStatus()).isEqualTo(FindArticleAppResponseStatus.NO_ARTICLE);
        assertThat(responseInvalidAuthor.getStatus()).isEqualTo(FindArticleAppResponseStatus.NO_ARTICLE);
        assertThat(responseInvalidVideo.getStatus()).isEqualTo(FindArticleAppResponseStatus.NO_ARTICLE);

        assertThat(responseByGuest.getStatus()).isEqualTo(FindArticleAppResponseStatus.SUCCESS);
        assertThat(responseByGuest.getId().orElseThrow()).isEqualTo(article.getId());
        assertThat(responseByGuest.getUser()).isEqualTo(userAppResponse);
        assertThat(responseByGuest.getVideo()).isEqualTo(videoAppResponse);
        assertThat(responseByGuest.getTitle()).isEqualTo(article.getTitle());
        assertThat(responseByGuest.getDescription()).isEqualTo(article.getDescription());
        assertThat(responseByGuest.getLevel()).isEqualTo(article.getLevel());
        assertThat(responseByGuest.getAngle()).isEqualTo(article.getAngle());
        assertThat(responseByGuest.getViewCount()).isEqualTo(article.getViewCount() - 1);
        assertThat(responseByGuest.getFavoriteCount()).isEqualTo(article.getFavoriteCount());
        assertThat(responseByGuest.getRegisterDateTime().orElseThrow()).isEqualTo(article.getRegisterDateTime());
        assertThat(responseByGuest.isFavorite()).isFalse();

        assertThat(responseByAuthor.getStatus()).isEqualTo(FindArticleAppResponseStatus.SUCCESS);
        assertThat(responseByAuthor.getViewCount()).isEqualTo(article.getViewCount() - 1);
        assertThat(responseByAuthor.getFavoriteCount()).isEqualTo(article.getFavoriteCount());
        assertThat(responseByAuthor.isFavorite()).isFalse();

        assertThat(responseByRequester.getStatus()).isEqualTo(FindArticleAppResponseStatus.SUCCESS);
        assertThat(responseByRequester.getViewCount()).isEqualTo(article.getViewCount());
        assertThat(responseByRequester.getFavoriteCount()).isEqualTo(article.getFavoriteCount());
        assertThat(responseByRequester.isFavorite()).isTrue();
    }

    @Test
    void write() {
        AtomicLong counter = new AtomicLong(0);

        User author = User.builder()
                .id(10L)
                .username("test")
                .build();

        Video videoWithPreprocessing = Video.builder()
                .id(100L)
                .uuid("unique")
                .status(VideoStatus.PREPROCESSING)
                .build();

        Video videoWithCertified = Video.builder()
                .id(101L)
                .uuid("good")
                .status(VideoStatus.CERTIFIED)
                .build();

        Video videoWithNoCertified = Video.builder()
                .id(102L)
                .uuid("bad")
                .status(VideoStatus.NO_CERTIFIED)
                .build();

        Video videoAlreadyPosted = Video.builder()
                .id(111L)
                .uuid("unique2")
                .build();

        WriteArticleAppRequest requestInvalidVideo = new WriteArticleAppRequest();
        requestInvalidVideo.setUsername("유저");
        requestInvalidVideo.setVideoUuid("동영상");
        requestInvalidVideo.setTitle("제목");
        requestInvalidVideo.setDescription("내용");
        requestInvalidVideo.setLevel(5);
        requestInvalidVideo.setAngle(70);

        WriteArticleAppRequest requestInvalidUser = new WriteArticleAppRequest();
        requestInvalidUser.setUsername("유저");
        requestInvalidUser.setVideoUuid(videoAlreadyPosted.getUuid());
        requestInvalidUser.setTitle("제목");
        requestInvalidUser.setDescription("내용");
        requestInvalidUser.setLevel(5);
        requestInvalidUser.setAngle(70);

        WriteArticleAppRequest requestAlreadyPostedVideo = new WriteArticleAppRequest();
        requestAlreadyPostedVideo.setUsername(author.getUsername());
        requestAlreadyPostedVideo.setVideoUuid(videoAlreadyPosted.getUuid());
        requestAlreadyPostedVideo.setTitle("제목");
        requestAlreadyPostedVideo.setDescription("내용");
        requestAlreadyPostedVideo.setLevel(5);
        requestAlreadyPostedVideo.setAngle(70);

        WriteArticleAppRequest requestWithPreprocessing = new WriteArticleAppRequest();
        requestWithPreprocessing.setUsername(author.getUsername());
        requestWithPreprocessing.setVideoUuid(videoWithPreprocessing.getUuid());
        requestWithPreprocessing.setTitle("제목");
        requestWithPreprocessing.setDescription("내용");
        requestWithPreprocessing.setLevel(5);
        requestWithPreprocessing.setAngle(70);

        WriteArticleAppRequest requestWithCertified = new WriteArticleAppRequest();
        requestWithCertified.setUsername(author.getUsername());
        requestWithCertified.setVideoUuid(videoWithCertified.getUuid());
        requestWithCertified.setTitle("제목2");
        requestWithCertified.setDescription("내용2");
        requestWithCertified.setLevel(5);
        requestWithCertified.setAngle(70);

        WriteArticleAppRequest requestWithNoCertified = new WriteArticleAppRequest();
        requestWithNoCertified.setUsername(author.getUsername());
        requestWithNoCertified.setVideoUuid(videoWithNoCertified.getUuid());
        requestWithNoCertified.setTitle("3");
        requestWithNoCertified.setDescription("3");
        requestWithNoCertified.setLevel(19);
        requestWithNoCertified.setAngle(90);

        given(this.articleRepository.existsByVideoId(any())).willReturn(false);
        given(this.articleRepository.existsByVideoId(videoAlreadyPosted.getId())).willReturn(true);
        willAnswer(invocation -> {
            Article article = invocation.getArgument(0);
            ReflectionTestUtils.setField(article, "id", counter.incrementAndGet());
            return null;
        }).given(this.articleRepository).saveAndFlush(any());

        given(this.userRepository.findForUpdateByUsername(any())).willReturn(Optional.empty());
        given(this.userRepository.findForUpdateByUsername(author.getUsername())).willReturn(Optional.of(author));

        given(this.videoRepository.findForShareByUuid(any())).willReturn(Optional.empty());
        given(this.videoRepository.findForShareByUuid(videoWithPreprocessing.getUuid()))
                .willReturn(Optional.of(videoWithPreprocessing));
        given(this.videoRepository.findForShareByUuid(videoWithCertified.getUuid()))
                .willReturn(Optional.of(videoWithCertified));
        given(this.videoRepository.findForShareByUuid(videoWithNoCertified.getUuid()))
                .willReturn(Optional.of(videoWithNoCertified));
        given(this.videoRepository.findForShareByUuid(videoAlreadyPosted.getUuid()))
                .willReturn(Optional.of(videoAlreadyPosted));

        WriteArticleAppResponse responseInvalidVideo = this.articleApplicationImpl.write(requestInvalidVideo);
        WriteArticleAppResponse responseInvalidUser = this.articleApplicationImpl.write(requestInvalidUser);
        WriteArticleAppResponse responseAlreadyPostedVideo =
                this.articleApplicationImpl.write(requestAlreadyPostedVideo);
        WriteArticleAppResponse responseWithPreprocessing = this.articleApplicationImpl.write(requestWithPreprocessing);
        WriteArticleAppResponse responseWithNoCertified = this.articleApplicationImpl.write(requestWithNoCertified);
        WriteArticleAppResponse responseWithCertified = this.articleApplicationImpl.write(requestWithCertified);

        then(this.articleRepository).should(times(3)).saveAndFlush(any());
        then(this.articleRepository).should(times(1)).saveAndFlush(argThat(
                argument -> argument.getUser() == author &&
                        argument.getVideo() == videoWithPreprocessing &&
                        argument.getTitle().equals(requestWithPreprocessing.getTitle()) &&
                        argument.getDescription().equals(requestWithPreprocessing.getDescription()) &&
                        argument.getLevel() == requestWithPreprocessing.getLevel() &&
                        argument.getAngle() == requestWithPreprocessing.getAngle() &&
                        argument.getViewCount() == 0 &&
                        argument.getFavoriteCount() == 0 &&
                        argument.getRegisterDateTime().compareTo(this.now) >= 0 &&
                        argument.getRegisterDateTime().compareTo(this.now.plusSeconds(5)) <= 0));
        then(this.articleRepository).should(times(1)).saveAndFlush(argThat(
                argument -> argument.getUser() == author &&
                        argument.getVideo() == videoWithNoCertified &&
                        argument.getTitle().equals(requestWithNoCertified.getTitle()) &&
                        argument.getDescription().equals(requestWithNoCertified.getDescription()) &&
                        argument.getLevel() == requestWithNoCertified.getLevel() &&
                        argument.getAngle() == requestWithNoCertified.getAngle() &&
                        argument.getViewCount() == 0 &&
                        argument.getFavoriteCount() == 0 &&
                        argument.getRegisterDateTime().compareTo(this.now) >= 0 &&
                        argument.getRegisterDateTime().compareTo(this.now.plusSeconds(5)) <= 0));
        then(this.articleRepository).should(times(1)).saveAndFlush(argThat(
                argument -> argument.getUser() == author &&
                        argument.getVideo() == videoWithCertified &&
                        argument.getTitle().equals(requestWithCertified.getTitle()) &&
                        argument.getDescription().equals(requestWithCertified.getDescription()) &&
                        argument.getLevel() == requestWithCertified.getLevel() &&
                        argument.getAngle() == requestWithCertified.getAngle() &&
                        argument.getViewCount() == 0 &&
                        argument.getFavoriteCount() == 0 &&
                        argument.getRegisterDateTime().compareTo(this.now) >= 0 &&
                        argument.getRegisterDateTime().compareTo(this.now.plusSeconds(5)) <= 0));

        then(this.userService).should(times(3)).noticeNewArticle(any());
        then(this.userService).should(times(3)).noticeNewArticle(author);
        then(this.userService).should(times(1)).noticeNewCertified(any());
        then(this.userService).should(times(1)).noticeNewCertified(author);

        assertThat(responseInvalidVideo.getStatus()).isEqualTo(WriteArticleAppResponseStatus.NO_VIDEO);
        assertThat(responseInvalidUser.getStatus()).isEqualTo(WriteArticleAppResponseStatus.NO_USER);
        assertThat(responseAlreadyPostedVideo.getStatus()).isEqualTo(
                WriteArticleAppResponseStatus.ALREADY_POSTED_VIDEO);

        assertThat(responseWithPreprocessing.getStatus()).isEqualTo(WriteArticleAppResponseStatus.SUCCESS);
        assertThat(responseWithPreprocessing.getId().orElseThrow()).isEqualTo(1L);

        assertThat(responseWithNoCertified.getStatus()).isEqualTo(WriteArticleAppResponseStatus.SUCCESS);
        assertThat(responseWithNoCertified.getId().orElseThrow()).isEqualTo(2L);

        assertThat(responseWithCertified.getStatus()).isEqualTo(WriteArticleAppResponseStatus.SUCCESS);
        assertThat(responseWithCertified.getId().orElseThrow()).isEqualTo(3L);
    }

    @Test
    void edit() {
        Article article = spy(Article.builder()
                .id(42L)
                .title("title")
                .description("description")
                .user(User.builder().username("me").build())
                .build());

        EditArticleAppRequest requestNoArticle = new EditArticleAppRequest();
        requestNoArticle.setArticleId(1L);
        requestNoArticle.setUsername(article.getUser().getUsername());
        requestNoArticle.setTitle("hello");
        requestNoArticle.setDescription("world");

        EditArticleAppRequest requestNotAuthor = new EditArticleAppRequest();
        requestNotAuthor.setArticleId(article.getId());
        requestNotAuthor.setUsername("no");
        requestNotAuthor.setTitle("hello2");
        requestNotAuthor.setDescription("world2");

        EditArticleAppRequest requestNoAuthorArticle = new EditArticleAppRequest();
        requestNoAuthorArticle.setArticleId(404L);
        requestNoAuthorArticle.setUsername(article.getUser().getUsername());
        requestNoAuthorArticle.setTitle("hello2");
        requestNoAuthorArticle.setDescription("world2");

        EditArticleAppRequest requestOk = new EditArticleAppRequest();
        requestOk.setArticleId(article.getId());
        requestOk.setUsername(article.getUser().getUsername());
        requestOk.setTitle("hello3");
        requestOk.setDescription("world3");

        given(this.articleRepository.findWithoutJoinForUpdateById(any())).willReturn(Optional.empty());
        given(this.articleRepository.findWithoutJoinForUpdateById(requestNoAuthorArticle.getArticleId()))
                .willReturn(Optional.of(Article.builder().build()));
        given(this.articleRepository.findWithoutJoinForUpdateById(article.getId())).willReturn(Optional.of(article));

        EditArticleAppResponse responseNoArticle = this.articleApplicationImpl.edit(requestNoArticle);
        EditArticleAppResponse responseNotAuthor = this.articleApplicationImpl.edit(requestNotAuthor);
        EditArticleAppResponse responseNoAuthorArticle = this.articleApplicationImpl.edit(requestNoAuthorArticle);
        EditArticleAppResponse responseOk = this.articleApplicationImpl.edit(requestOk);

        then(article).should(times(1)).edit(any(), any());
        then(article).should(times(1)).edit(requestOk.getTitle(), requestOk.getDescription());
        then(this.articleRepository).should(times(1)).save(any());
        then(this.articleRepository).should(times(1)).save(article);

        assertThat(responseNoArticle.getStatus()).isEqualTo(EditArticleAppResponseStatus.NO_ARTICLE);
        assertThat(responseNotAuthor.getStatus()).isEqualTo(EditArticleAppResponseStatus.FORBIDDEN);
        assertThat(responseNoAuthorArticle.getStatus()).isEqualTo(EditArticleAppResponseStatus.FORBIDDEN);
        assertThat(responseOk.getStatus()).isEqualTo(EditArticleAppResponseStatus.SUCCESS);
    }

    @Test
    void delete() {
        User author = User.builder().username("me").build();

        Video videoWithPreprocessing = Video.builder().uuid("test").status(VideoStatus.PREPROCESSING).build();
        Video videoWithNoCertified = Video.builder().uuid("test2").status(VideoStatus.NO_CERTIFIED).build();
        Video videoWithCertified = Video.builder().uuid("test3").status(VideoStatus.CERTIFIED).build();

        Article articleWithPreprocessing = Article.builder()
                .id(42L)
                .title("title")
                .description("description")
                .user(author)
                .video(videoWithPreprocessing)
                .build();

        Article articleWithNoCertified = Article.builder()
                .id(43L)
                .title("title")
                .description("description")
                .user(author)
                .video(videoWithNoCertified)
                .build();

        Article articleWithCertified = Article.builder()
                .id(44L)
                .title("title")
                .description("description")
                .user(author)
                .video(videoWithCertified)
                .build();

        DeleteArticleAppRequest requestNoArticle = new DeleteArticleAppRequest();
        requestNoArticle.setArticleId(1L);
        requestNoArticle.setUsername(articleWithCertified.getUser().getUsername());

        DeleteArticleAppRequest requestNotAuthor = new DeleteArticleAppRequest();
        requestNotAuthor.setArticleId(articleWithCertified.getId());
        requestNotAuthor.setUsername("no");

        DeleteArticleAppRequest requestNoAuthorArticle = new DeleteArticleAppRequest();
        requestNoAuthorArticle.setArticleId(404L);
        requestNoAuthorArticle.setUsername(articleWithCertified.getUser().getUsername());

        DeleteArticleAppRequest requestWithPreprocessing = new DeleteArticleAppRequest();
        requestWithPreprocessing.setArticleId(articleWithPreprocessing.getId());
        requestWithPreprocessing.setUsername(articleWithPreprocessing.getUser().getUsername());

        DeleteArticleAppRequest requestWithNoCertified = new DeleteArticleAppRequest();
        requestWithNoCertified.setArticleId(articleWithNoCertified.getId());
        requestWithNoCertified.setUsername(articleWithNoCertified.getUser().getUsername());

        DeleteArticleAppRequest requestWithCertified = new DeleteArticleAppRequest();
        requestWithCertified.setArticleId(articleWithCertified.getId());
        requestWithCertified.setUsername(articleWithCertified.getUser().getUsername());

        given(this.articleRepository.findForUpdateById(any())).willReturn(Optional.empty());
        given(this.articleRepository.findForUpdateById(requestNoAuthorArticle.getArticleId()))
                .willReturn(Optional.of(Article.builder().build()));
        given(this.articleRepository.findForUpdateById(articleWithPreprocessing.getId()))
                .willReturn(Optional.of(articleWithPreprocessing));
        given(this.articleRepository.findForUpdateById(articleWithNoCertified.getId()))
                .willReturn(Optional.of(articleWithNoCertified));
        given(this.articleRepository.findForUpdateById(articleWithCertified.getId()))
                .willReturn(Optional.of(articleWithCertified));

        DeleteArticleAppResponse responseNoArticle = this.articleApplicationImpl.delete(requestNoArticle);
        DeleteArticleAppResponse responseNotAuthor = this.articleApplicationImpl.delete(requestNotAuthor);
        DeleteArticleAppResponse responseNoAuthorArticle = this.articleApplicationImpl.delete(requestNoAuthorArticle);
        DeleteArticleAppResponse responseWithPreprocessing =
                this.articleApplicationImpl.delete(requestWithPreprocessing);
        DeleteArticleAppResponse responseWithNoCertified = this.articleApplicationImpl.delete(requestWithNoCertified);
        DeleteArticleAppResponse responseWithCertified = this.articleApplicationImpl.delete(requestWithCertified);

        then(this.articleRepository).should(times(3)).delete(any());
        then(this.articleRepository).should(times(1)).delete(articleWithPreprocessing);
        then(this.articleRepository).should(times(1)).delete(articleWithNoCertified);
        then(this.articleRepository).should(times(1)).delete(articleWithCertified);

        then(this.videoRepository).should(times(3)).delete(any());
        then(this.videoRepository).should(times(1)).delete(videoWithPreprocessing);
        then(this.videoRepository).should(times(1)).delete(videoWithNoCertified);
        then(this.videoRepository).should(times(1)).delete(videoWithCertified);

        then(this.userService).should(times(3)).noticeDeleteArticle(any());
        then(this.userService).should(times(3)).noticeDeleteArticle(author);
        then(this.userService).should(times(1)).noticeDeleteCertified(any());
        then(this.userService).should(times(1)).noticeDeleteCertified(author);

        assertThat(responseNoArticle.getStatus()).isEqualTo(DeleteArticleAppResponseStatus.NO_ARTICLE);
        assertThat(responseNotAuthor.getStatus()).isEqualTo(DeleteArticleAppResponseStatus.FORBIDDEN);
        assertThat(responseNoAuthorArticle.getStatus()).isEqualTo(DeleteArticleAppResponseStatus.FORBIDDEN);
        assertThat(responseWithPreprocessing.getStatus()).isEqualTo(DeleteArticleAppResponseStatus.SUCCESS);
        assertThat(responseWithNoCertified.getStatus()).isEqualTo(DeleteArticleAppResponseStatus.SUCCESS);
        assertThat(responseWithCertified.getStatus()).isEqualTo(DeleteArticleAppResponseStatus.SUCCESS);
    }

    @Test
    void react() {
        User author = User.builder()
                .id(10L)
                .username("test")
                .build();

        User requesterAlreadyFavorite = User.builder()
                .id(20L)
                .username("test2")
                .build();

        User requesterNotFavorite = User.builder()
                .id(21L)
                .username("test3")
                .build();

        Article article = Article.builder()
                .id(1000L)
                .user(author)
                .viewCount(3282)
                .favoriteCount(672)
                .build();

        ReactArticleAppRequest requestInvalidArticle = new ReactArticleAppRequest();
        requestInvalidArticle.setUsernameRequestedBy("hi");
        requestInvalidArticle.setArticleId(404L);
        requestInvalidArticle.setFavorite(false);

        ReactArticleAppRequest requestInvalidUser = new ReactArticleAppRequest();
        requestInvalidUser.setUsernameRequestedBy("hi");
        requestInvalidUser.setArticleId(article.getId());
        requestInvalidUser.setFavorite(true);

        ReactArticleAppRequest requestAuthorDoFavor = new ReactArticleAppRequest();
        requestAuthorDoFavor.setUsernameRequestedBy(author.getUsername());
        requestAuthorDoFavor.setArticleId(article.getId());
        requestAuthorDoFavor.setFavorite(true);

        ReactArticleAppRequest requestAuthorDoNotFavor = new ReactArticleAppRequest();
        requestAuthorDoNotFavor.setUsernameRequestedBy(author.getUsername());
        requestAuthorDoNotFavor.setArticleId(article.getId());
        requestAuthorDoNotFavor.setFavorite(false);

        ReactArticleAppRequest requestAlreadyFavoriteDoFavor = new ReactArticleAppRequest();
        requestAlreadyFavoriteDoFavor.setUsernameRequestedBy(requesterAlreadyFavorite.getUsername());
        requestAlreadyFavoriteDoFavor.setArticleId(article.getId());
        requestAlreadyFavoriteDoFavor.setFavorite(true);

        ReactArticleAppRequest requestAlreadyFavoriteDoNotFavor = new ReactArticleAppRequest();
        requestAlreadyFavoriteDoNotFavor.setUsernameRequestedBy(requesterAlreadyFavorite.getUsername());
        requestAlreadyFavoriteDoNotFavor.setArticleId(article.getId());
        requestAlreadyFavoriteDoNotFavor.setFavorite(false);

        ReactArticleAppRequest requestNotFavoriteDoFavor = new ReactArticleAppRequest();
        requestNotFavoriteDoFavor.setUsernameRequestedBy(requesterNotFavorite.getUsername());
        requestNotFavoriteDoFavor.setArticleId(article.getId());
        requestNotFavoriteDoFavor.setFavorite(true);

        ReactArticleAppRequest requestNotFavoriteDoNotFavor = new ReactArticleAppRequest();
        requestNotFavoriteDoNotFavor.setUsernameRequestedBy(requesterNotFavorite.getUsername());
        requestNotFavoriteDoNotFavor.setArticleId(article.getId());
        requestNotFavoriteDoNotFavor.setFavorite(false);

        given(this.articleRepository.findById(any())).willReturn(Optional.empty());
        given(this.articleRepository.findById(article.getId())).willReturn(Optional.of(article));

        given(this.articleFavoriteRepository.findForUpdateByArticleIdAndUserId(any(), any()))
                .willReturn(Optional.empty());
        given(this.articleFavoriteRepository.findForUpdateByArticleIdAndUserId(article.getId(),
                requesterAlreadyFavorite.getId()))
                .willReturn(Optional.of(ArticleFavorite.builder()
                        .article(article).user(requesterAlreadyFavorite).build()));

        given(this.userRepository.findByUsername(any())).willReturn(Optional.empty());
        given(this.userRepository.findByUsername(author.getUsername())).willReturn(Optional.of(author));
        given(this.userRepository.findByUsername(requesterAlreadyFavorite.getUsername()))
                .willReturn(Optional.of(requesterAlreadyFavorite));
        given(this.userRepository.findByUsername(requesterNotFavorite.getUsername()))
                .willReturn(Optional.of(requesterNotFavorite));

        ReactArticleAppResponse responseInvalidArticle = this.articleApplicationImpl.react(requestInvalidArticle);
        ReactArticleAppResponse responseInvalidUser = this.articleApplicationImpl.react(requestInvalidUser);
        ReactArticleAppResponse responseAuthorDoFavor = this.articleApplicationImpl.react(requestAuthorDoFavor);
        ReactArticleAppResponse responseAuthorDoNotFavor = this.articleApplicationImpl.react(requestAuthorDoNotFavor);
        ReactArticleAppResponse responseAlreadyFavoriteDoFavor =
                this.articleApplicationImpl.react(requestAlreadyFavoriteDoFavor);
        ReactArticleAppResponse responseAlreadyFavoriteDoNotFavor =
                this.articleApplicationImpl.react(requestAlreadyFavoriteDoNotFavor);
        ReactArticleAppResponse responseNotFavoriteDoFavor =
                this.articleApplicationImpl.react(requestNotFavoriteDoFavor);
        ReactArticleAppResponse responseNotFavoriteDoNotFavor =
                this.articleApplicationImpl.react(requestNotFavoriteDoNotFavor);

        then(this.articleRepository).should(times(2)).incrementFavoriteCountById(any());
        then(this.articleRepository).should(times(2)).incrementFavoriteCountById(article.getId());

        then(this.articleRepository).should(times(1)).decrementFavoriteCountById(any());
        then(this.articleRepository).should(times(1)).decrementFavoriteCountById(article.getId());

        then(this.articleFavoriteRepository).should(times(2)).save(any());
        then(this.articleFavoriteRepository).should(times(1))
                .save(argThat(argument -> argument.getArticle() == article && argument.getUser() == author));
        then(this.articleFavoriteRepository).should(times(1))
                .save(argThat(argument -> argument.getArticle() == article &&
                        argument.getUser() == requesterNotFavorite));

        then(this.articleFavoriteRepository).should(times(1)).delete(any());
        then(this.articleFavoriteRepository).should(times(1))
                .delete(argThat(argument -> argument.getArticle() == article &&
                        argument.getUser() == requesterAlreadyFavorite));

        assertThat(responseInvalidArticle.getStatus()).isEqualTo(ReactArticleAppResponseStatus.NO_ARTICLE);

        assertThat(responseInvalidUser.getStatus()).isEqualTo(ReactArticleAppResponseStatus.SUCCESS);
        assertThat(responseInvalidUser.isFavorite()).isTrue();

        assertThat(responseAuthorDoFavor.getStatus()).isEqualTo(ReactArticleAppResponseStatus.SUCCESS);
        assertThat(responseAuthorDoFavor.isFavorite()).isTrue();

        assertThat(responseAuthorDoNotFavor.getStatus()).isEqualTo(ReactArticleAppResponseStatus.SUCCESS);
        assertThat(responseAuthorDoNotFavor.isFavorite()).isFalse();

        assertThat(responseAlreadyFavoriteDoFavor.getStatus()).isEqualTo(ReactArticleAppResponseStatus.SUCCESS);
        assertThat(responseAlreadyFavoriteDoFavor.isFavorite()).isTrue();

        assertThat(responseAlreadyFavoriteDoNotFavor.getStatus()).isEqualTo(ReactArticleAppResponseStatus.SUCCESS);
        assertThat(responseAlreadyFavoriteDoNotFavor.isFavorite()).isFalse();

        assertThat(responseNotFavoriteDoFavor.getStatus()).isEqualTo(ReactArticleAppResponseStatus.SUCCESS);
        assertThat(responseNotFavoriteDoFavor.isFavorite()).isTrue();

        assertThat(responseNotFavoriteDoNotFavor.getStatus()).isEqualTo(ReactArticleAppResponseStatus.SUCCESS);
        assertThat(responseNotFavoriteDoNotFavor.isFavorite()).isFalse();
    }

    @Test
    void search() {
    }
}
