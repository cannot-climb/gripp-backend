package kr.njw.gripp.user.controller;

import kr.njw.gripp.user.application.UserApplication;
import kr.njw.gripp.user.application.dto.FindLeaderBoardAppResponse;
import kr.njw.gripp.user.application.dto.FindUserAppResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private UserApplication userApplication;
    private Random random;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        this.random = new Random(42);
        this.now = LocalDateTime.now();
    }

    @AfterEach
    void tearDown() {
    }

    @WithMockUser
    @Test
    void findUser() throws Exception {
        FindUserAppResponse appResponse = new FindUserAppResponse();
        appResponse.setSuccess(true);
        appResponse.setUsername("njw1204");
        appResponse.setTier(14);
        appResponse.setScore(1432);
        appResponse.setRank(32);
        appResponse.setPercentile(99);
        appResponse.setArticleCount(2);
        appResponse.setArticleCertifiedCount(1);
        appResponse.setRegisterDateTime(this.now);

        given(this.userApplication.findUser(anyString())).willReturn(new FindUserAppResponse());
        given(this.userApplication.findUser(appResponse.getUsername().orElseThrow())).willReturn(appResponse);

        ResultActions perform = this.mockMvc.perform(get("/users/test"));
        ResultActions perform2 = this.mockMvc.perform(get("/users/" + appResponse.getUsername().orElseThrow()));

        perform.andExpect(status().isNotFound());
        perform2.andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.username").value(appResponse.getUsername().orElseThrow()))
                .andExpect(jsonPath("$.tier").value(appResponse.getTier()))
                .andExpect(jsonPath("$.score").value(appResponse.getScore() / 100.0))
                .andExpect(jsonPath("$.rank").value(appResponse.getRank()))
                .andExpect(jsonPath("$.percentile").value(appResponse.getPercentile()))
                .andExpect(jsonPath("$.articleCount").value(appResponse.getArticleCount()))
                .andExpect(jsonPath("$.articleCertifiedCount").value(appResponse.getArticleCertifiedCount()))
                .andExpect(jsonPath("$.registerDateTime").value(appResponse.getRegisterDateTime().orElseThrow().format(
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
    }

    @WithMockUser
    @Test
    void findLeaderBoard() throws Exception {
        List<FindUserAppResponse> topBoard = new ArrayList<>();
        List<FindUserAppResponse> defaultBoard = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            topBoard.add(this.createFindUserAppResponse("user" + i));
        }

        for (int i = 0; i < 21; i++) {
            defaultBoard.add(this.createFindUserAppResponse("user" + i));
        }

        FindLeaderBoardAppResponse appResponse = new FindLeaderBoardAppResponse();
        appResponse.setSuccess(true);
        appResponse.setTopBoard(topBoard);
        appResponse.setDefaultBoard(defaultBoard);

        given(this.userApplication.findLeaderBoard(anyString())).willReturn(new FindLeaderBoardAppResponse());
        given(this.userApplication.findLeaderBoard("test")).willReturn(appResponse);

        ResultActions perform = this.mockMvc.perform(get("/users/tes/leaderboard"));
        ResultActions perform2 = this.mockMvc.perform(get("/users/test/leaderboard"));

        perform.andExpect(status().isNotFound());
        perform2.andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.topBoard").value(hasSize(10)))
                .andExpect(jsonPath("$.topBoard[*].username").value(
                        contains(topBoard.stream().map(user -> user.getUsername().orElseThrow()).toArray())))
                .andExpect(jsonPath("$.defaultBoard").value(hasSize(21)))
                .andExpect(jsonPath("$.defaultBoard[*].username").value(
                        contains(defaultBoard.stream().map(user -> user.getUsername().orElseThrow()).toArray())));
    }

    private FindUserAppResponse createFindUserAppResponse(String username) {
        FindUserAppResponse appResponse = new FindUserAppResponse();
        appResponse.setSuccess(true);
        appResponse.setUsername(username);
        appResponse.setTier(this.random.nextInt(0, 19 + 1));
        appResponse.setScore(this.random.nextInt(0, 1900 + 1));
        appResponse.setRank(this.random.nextInt(1, 100 + 1));
        appResponse.setPercentile(this.random.nextInt(0, 100 + 1));
        appResponse.setArticleCount(this.random.nextInt(0, 1000 + 1));
        appResponse.setArticleCertifiedCount(this.random.nextInt(0, appResponse.getArticleCount() + 1));
        appResponse.setRegisterDateTime(this.now);
        return appResponse;
    }
}
