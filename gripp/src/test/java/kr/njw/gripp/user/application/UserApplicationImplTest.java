package kr.njw.gripp.user.application;

import kr.njw.gripp.user.application.dto.FindLeaderBoardAppResponse;
import kr.njw.gripp.user.application.dto.FindUserAppResponse;
import kr.njw.gripp.user.entity.User;
import kr.njw.gripp.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class UserApplicationImplTest {
    @InjectMocks
    private UserApplicationImpl userApplicationImpl;
    @Mock
    private UserRepository userRepository;
    @Mock
    private EntityManager entityManager;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        this.now = LocalDateTime.now();
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void findUser() {
        User user = User.builder()
                .id(1L)
                .username("test")
                .score(100)
                .articleCount(10)
                .articleCertifiedCount(5)
                .registerDateTime(this.now)
                .build();
        User user2 = User.builder()
                .id(2L)
                .username("okay")
                .score(10)
                .articleCount(10)
                .articleCertifiedCount(5)
                .registerDateTime(this.now)
                .build();

        given(this.userRepository.findByUsername(anyString())).willReturn(Optional.empty());
        given(this.userRepository.findByUsername(user.getUsername())).willReturn(Optional.of(user));
        given(this.userRepository.findByUsername(user2.getUsername())).willReturn(Optional.of(user2));
        given(this.userRepository.countByScoreGreaterThan(0)).willReturn(100L);
        given(this.userRepository.countByScoreGreaterThan(user.getScore())).willReturn(10L);
        given(this.userRepository.countByScoreGreaterThan(user2.getScore())).willReturn(150L);

        FindUserAppResponse test = this.userApplicationImpl.findUser(user.getUsername());
        FindUserAppResponse test2 = this.userApplicationImpl.findUser(user2.getUsername());
        FindUserAppResponse test3 = this.userApplicationImpl.findUser("test3");

        then(this.userRepository).should(times(1)).findByUsername(user.getUsername());
        then(this.userRepository).should(times(1)).findByUsername(user2.getUsername());
        then(this.userRepository).should(times(1)).findByUsername("test3");
        then(this.userRepository).should(times(2)).countByScoreGreaterThan(0);
        then(this.userRepository).should(times(1)).countByScoreGreaterThan(user.getScore());
        then(this.userRepository).should(times(1)).countByScoreGreaterThan(user2.getScore());

        assertThat(test.isSuccess()).isTrue();
        assertThat(test.getUsername()).hasValue(user.getUsername());
        assertThat(test.getTier()).isEqualTo(user.getTier());
        assertThat(test.getScore()).isEqualTo(user.getScore());
        assertThat(test.getRank()).isEqualTo(11);
        assertThat(test.getPercentile()).isEqualTo(89);
        assertThat(test.getArticleCount()).isEqualTo(user.getArticleCount());
        assertThat(test.getArticleCertifiedCount()).isEqualTo(user.getArticleCertifiedCount());
        assertThat(test.getRegisterDateTime().orElseThrow()).isEqualTo(this.now);

        assertThat(test2.isSuccess()).isTrue();
        assertThat(test2.getRank()).isEqualTo(151);
        assertThat(test2.getPercentile()).isEqualTo(0);

        assertThat(test3.isSuccess()).isFalse();
        assertThat(test3.getUsername()).isEmpty();
        assertThat(test3.getRegisterDateTime()).isEmpty();
    }

    @Test
    void findLeaderBoard() {
        final long USER_COUNT = 10000;
        List<User> users = new ArrayList<>();

        for (long i = 0; i < USER_COUNT; i++) {
            users.add(User.builder()
                    .id(i)
                    .username("test" + i)
                    .score((int) (1900 * ((double) (USER_COUNT - i) / USER_COUNT)))
                    .articleCount((int) i)
                    .articleCertifiedCount((int) (i / 2))
                    .registerDateTime(this.now)
                    .build());
        }

        given(this.userRepository.countByScoreGreaterThan(0)).willReturn(USER_COUNT);
        given(this.userRepository.findByUsername(anyString())).willAnswer(invocation -> users.stream()
                .filter(user -> user.getUsername().equals(invocation.getArgument(0)))
                .findFirst());
        given(this.userRepository.findByOrderByScoreDescIdAsc()).willAnswer(invocation -> users.stream());

        FindLeaderBoardAppResponse test = this.userApplicationImpl.findLeaderBoard("test0");
        FindLeaderBoardAppResponse test2 = this.userApplicationImpl.findLeaderBoard("test10");
        FindLeaderBoardAppResponse test3 = this.userApplicationImpl.findLeaderBoard("test" + (USER_COUNT - 1));
        FindLeaderBoardAppResponse test4 = this.userApplicationImpl.findLeaderBoard("tes");

        assertThat(test.isSuccess()).isTrue();
        assertThat(test.getTopBoard()).hasSize(10);
        assertThat(test.getTopBoard()).filteredOn(appResponse ->
                appResponse.getUsername().orElseThrow().equals("test0")).hasSize(1);
        assertThat(test.getDefaultBoard()).hasSize(11);
        assertThat(test.getDefaultBoard()).filteredOn(appResponse ->
                appResponse.getUsername().orElseThrow().equals("test0")).hasSize(1);

        assertThat(test2.isSuccess()).isTrue();
        assertThat(test2.getTopBoard()).hasSize(10);
        assertThat(test2.getTopBoard()).filteredOn(appResponse ->
                appResponse.getUsername().orElseThrow().equals("test10")).isEmpty();
        assertThat(test2.getDefaultBoard()).hasSize(21);
        assertThat(test2.getDefaultBoard()).filteredOn(appResponse ->
                appResponse.getUsername().orElseThrow().equals("test10")).hasSize(1);

        assertThat(test3.isSuccess()).isTrue();
        assertThat(test3.getTopBoard()).hasSize(10);
        assertThat(test3.getTopBoard()).filteredOn(appResponse ->
                appResponse.getUsername().orElseThrow().equals("test10")).isEmpty();
        assertThat(test3.getDefaultBoard()).hasSize(11);
        assertThat(test3.getDefaultBoard()).filteredOn(appResponse ->
                appResponse.getUsername().orElseThrow().equals("test" + (USER_COUNT - 1))).hasSize(1);

        assertThat(test4.isSuccess()).isFalse();
    }
}
