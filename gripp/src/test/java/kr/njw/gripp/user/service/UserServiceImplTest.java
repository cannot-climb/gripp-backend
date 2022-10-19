package kr.njw.gripp.user.service;

import kr.njw.gripp.article.repository.ArticleRepository;
import kr.njw.gripp.user.entity.User;
import kr.njw.gripp.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {
    @InjectMocks
    private UserServiceImpl userServiceImpl;
    @Mock
    private ArticleRepository articleRepository;
    @Mock
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void noticeNewArticle() {
        User user = spy(User.builder().articleCount(0).build());

        this.userServiceImpl.noticeNewArticle(null);
        this.userServiceImpl.noticeNewArticle(user);
        int articleCount = user.getArticleCount();
        this.userServiceImpl.noticeNewArticle(user);
        int articleCount2 = user.getArticleCount();

        then(user).should(never()).submitScore(any());
        then(this.userRepository).should(times(2)).save(user);
        assertThat(articleCount).isEqualTo(1);
        assertThat(articleCount2).isEqualTo(2);
    }

    @Test
    void noticeNewCertified() {
        User user = spy(User.builder().articleCertifiedCount(0).build());

        this.userServiceImpl.noticeNewCertified(null);
        this.userServiceImpl.noticeNewCertified(user);
        int articleCertifiedCount = user.getArticleCertifiedCount();
        this.userServiceImpl.noticeNewCertified(user);
        int articleCertifiedCount2 = user.getArticleCertifiedCount();

        then(user).should(times(2)).submitScore(anyCollection());
        then(this.userRepository).should(times(2)).save(user);
        assertThat(articleCertifiedCount).isEqualTo(1);
        assertThat(articleCertifiedCount2).isEqualTo(2);
    }

    @Test
    void noticeDeleteArticle() {
        User user = spy(User.builder().articleCount(1).build());

        this.userServiceImpl.noticeDeleteArticle(null);
        this.userServiceImpl.noticeDeleteArticle(user);
        int articleCount = user.getArticleCount();
        this.userServiceImpl.noticeDeleteArticle(user);
        int articleCount2 = user.getArticleCount();

        then(user).should(never()).submitScore(any());
        then(this.userRepository).should(times(2)).save(user);
        assertThat(articleCount).isEqualTo(0);
        assertThat(articleCount2).isEqualTo(0);
    }

    @Test
    void noticeDeleteCertified() {
        User user = spy(User.builder().articleCertifiedCount(1).build());

        this.userServiceImpl.noticeDeleteCertified(null);
        this.userServiceImpl.noticeDeleteCertified(user);
        int articleCertifiedCount = user.getArticleCertifiedCount();
        this.userServiceImpl.noticeDeleteCertified(user);
        int articleCertifiedCount2 = user.getArticleCertifiedCount();

        then(user).should(times(2)).submitScore(anyCollection());
        then(this.userRepository).should(times(2)).save(user);
        assertThat(articleCertifiedCount).isEqualTo(0);
        assertThat(articleCertifiedCount2).isEqualTo(0);
    }
}
