package kr.njw.gripp.article.application;

import kr.njw.gripp.article.application.dto.EditArticleAppRequest;
import kr.njw.gripp.article.application.dto.EditArticleAppResponse;
import kr.njw.gripp.article.application.dto.EditArticleAppResponseStatus;
import kr.njw.gripp.article.entity.Article;
import kr.njw.gripp.article.repository.ArticleFavoriteRepository;
import kr.njw.gripp.article.repository.ArticleRepository;
import kr.njw.gripp.user.application.UserApplication;
import kr.njw.gripp.user.entity.User;
import kr.njw.gripp.user.repository.UserRepository;
import kr.njw.gripp.user.service.UserService;
import kr.njw.gripp.video.application.VideoApplication;
import kr.njw.gripp.video.repository.VideoRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
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

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void find() {
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
