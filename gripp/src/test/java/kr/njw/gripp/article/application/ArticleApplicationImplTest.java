package kr.njw.gripp.article.application;

import kr.njw.gripp.article.application.dto.*;
import kr.njw.gripp.article.entity.Article;
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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

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

        Article[] article = {Article.builder()
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
                .build()};

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
        requestByGuest.setArticleId(article[0].getId());
        requestByGuest.setUsernameRequestedBy("guest");

        FindArticleAppRequest requestByAuthor = new FindArticleAppRequest();
        requestByAuthor.setArticleId(article[0].getId());
        requestByAuthor.setUsernameRequestedBy(author.getUsername());

        FindArticleAppRequest requestByRequester = new FindArticleAppRequest();
        requestByRequester.setArticleId(article[0].getId());
        requestByRequester.setUsernameRequestedBy(requester.getUsername());

        given(this.articleRepository.findById(any())).willReturn(Optional.empty());
        given(this.articleRepository.findById(article[0].getId())).willAnswer(invocation -> Optional.of(article[0]));
        given(this.articleRepository.findById(articleInvalidUser.getId())).willReturn(Optional.of(articleInvalidUser));
        given(this.articleRepository.findById(articleInvalidVid.getId())).willReturn(Optional.of(articleInvalidVid));
        willAnswer(invocation -> {
            article[0] = article[0].toBuilder().viewCount(article[0].getViewCount() + 1).build();
            return null;
        }).given(this.articleRepository).incrementViewCountById(article[0].getId());

        given(this.articleFavoriteRepository.existsByArticleIdAndUserId(any(), any())).willReturn(false);
        given(this.articleFavoriteRepository.existsByArticleIdAndUserId(article[0].getId(), requester.getId()))
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
        then(this.articleRepository).should(times(1)).incrementViewCountById(article[0].getId());

        assertThat(responseNoArticle.getStatus()).isEqualTo(FindArticleAppResponseStatus.NO_ARTICLE);
        assertThat(responseInvalidAuthor.getStatus()).isEqualTo(FindArticleAppResponseStatus.NO_ARTICLE);
        assertThat(responseInvalidVideo.getStatus()).isEqualTo(FindArticleAppResponseStatus.NO_ARTICLE);

        assertThat(responseByGuest.getStatus()).isEqualTo(FindArticleAppResponseStatus.SUCCESS);
        assertThat(responseByGuest.getId().orElseThrow()).isEqualTo(article[0].getId());
        assertThat(responseByGuest.getUser()).isEqualTo(userAppResponse);
        assertThat(responseByGuest.getVideo()).isEqualTo(videoAppResponse);
        assertThat(responseByGuest.getTitle()).isEqualTo(article[0].getTitle());
        assertThat(responseByGuest.getDescription()).isEqualTo(article[0].getDescription());
        assertThat(responseByGuest.getLevel()).isEqualTo(article[0].getLevel());
        assertThat(responseByGuest.getAngle()).isEqualTo(article[0].getAngle());
        assertThat(responseByGuest.getViewCount()).isEqualTo(article[0].getViewCount() - 1);
        assertThat(responseByGuest.getFavoriteCount()).isEqualTo(article[0].getFavoriteCount());
        assertThat(responseByGuest.getRegisterDateTime().orElseThrow()).isEqualTo(article[0].getRegisterDateTime());
        assertThat(responseByGuest.isFavorite()).isFalse();

        assertThat(responseByAuthor.getStatus()).isEqualTo(FindArticleAppResponseStatus.SUCCESS);
        assertThat(responseByAuthor.getViewCount()).isEqualTo(article[0].getViewCount() - 1);
        assertThat(responseByAuthor.getFavoriteCount()).isEqualTo(article[0].getFavoriteCount());
        assertThat(responseByAuthor.isFavorite()).isFalse();

        assertThat(responseByRequester.getStatus()).isEqualTo(FindArticleAppResponseStatus.SUCCESS);
        assertThat(responseByRequester.getViewCount()).isEqualTo(article[0].getViewCount());
        assertThat(responseByRequester.getFavoriteCount()).isEqualTo(article[0].getFavoriteCount());
        assertThat(responseByRequester.isFavorite()).isTrue();
    }

    @Test
    void write() {
    }

    @Test
    void edit() {
        Article article = spy(Article.builder()
                .id(42L)
                .title("title")
                .description("description")
                .user(User.builder().username("me").build())
                .build());

        EditArticleAppRequest request = new EditArticleAppRequest();
        request.setArticleId(1L);
        request.setUsername(article.getUser().getUsername());
        request.setTitle("hello");
        request.setDescription("world");

        EditArticleAppRequest request2 = new EditArticleAppRequest();
        request2.setArticleId(article.getId());
        request2.setUsername("no");
        request2.setTitle("hello2");
        request2.setDescription("world2");

        EditArticleAppRequest request3 = new EditArticleAppRequest();
        request3.setArticleId(404L);
        request3.setUsername(article.getUser().getUsername());
        request3.setTitle("hello2");
        request3.setDescription("world2");

        EditArticleAppRequest request4 = new EditArticleAppRequest();
        request4.setArticleId(article.getId());
        request4.setUsername(article.getUser().getUsername());
        request4.setTitle("hello3");
        request4.setDescription("world3");

        given(this.articleRepository.findWithoutJoinForUpdateById(any())).willReturn(Optional.empty());
        given(this.articleRepository.findWithoutJoinForUpdateById(404L)).willReturn(
                Optional.of(Article.builder().build()));
        given(this.articleRepository.findWithoutJoinForUpdateById(article.getId())).willReturn(Optional.of(article));

        EditArticleAppResponse response = this.articleApplicationImpl.edit(request);
        EditArticleAppResponse response2 = this.articleApplicationImpl.edit(request2);
        EditArticleAppResponse response3 = this.articleApplicationImpl.edit(request3);
        EditArticleAppResponse response4 = this.articleApplicationImpl.edit(request4);

        then(article).should(times(1)).edit(any(), any());
        then(article).should(times(1)).edit(request4.getTitle(), request4.getDescription());
        then(this.articleRepository).should(times(1)).save(any());
        then(this.articleRepository).should(times(1)).save(article);

        assertThat(response.getStatus()).isEqualTo(EditArticleAppResponseStatus.NO_ARTICLE);
        assertThat(response2.getStatus()).isEqualTo(EditArticleAppResponseStatus.FORBIDDEN);
        assertThat(response3.getStatus()).isEqualTo(EditArticleAppResponseStatus.FORBIDDEN);
        assertThat(response4.getStatus()).isEqualTo(EditArticleAppResponseStatus.SUCCESS);
    }

    @Test
    void delete() {
    }

    @Test
    void react() {
    }

    @Test
    void search() {
    }
}
