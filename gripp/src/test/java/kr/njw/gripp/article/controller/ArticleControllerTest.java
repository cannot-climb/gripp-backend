package kr.njw.gripp.article.controller;

import kr.njw.gripp.article.application.ArticleApplication;
import kr.njw.gripp.article.application.dto.EditArticleAppResponse;
import kr.njw.gripp.article.application.dto.EditArticleAppResponseStatus;
import kr.njw.gripp.article.application.dto.FindArticleAppResponse;
import kr.njw.gripp.article.application.dto.FindArticleAppResponseStatus;
import kr.njw.gripp.user.application.dto.FindUserAppResponse;
import kr.njw.gripp.video.application.dto.FindVideoAppResponse;
import kr.njw.gripp.video.entity.vo.VideoStatus;
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

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ArticleController.class)
class ArticleControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private ArticleApplication articleApplication;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        this.now = LocalDateTime.now();
    }

    @AfterEach
    void tearDown() {
    }

    @WithMockUser
    @Test
    void findArticle() throws Exception {
        FindUserAppResponse userAppResponse = new FindUserAppResponse();
        userAppResponse.setSuccess(true);
        userAppResponse.setUsername("njw1204");
        userAppResponse.setTier(14);
        userAppResponse.setScore(1432);
        userAppResponse.setRank(32);
        userAppResponse.setPercentile(99);
        userAppResponse.setArticleCount(2);
        userAppResponse.setArticleCertifiedCount(1);
        userAppResponse.setRegisterDateTime(this.now);

        FindVideoAppResponse videoAppResponse = new FindVideoAppResponse();
        videoAppResponse.setSuccess(true);
        videoAppResponse.setUuid("ko");
        videoAppResponse.setStreamingUrl("http://stream.com/movie.mp4");
        videoAppResponse.setStreamingLength(10);
        videoAppResponse.setStreamingAspectRatio(1.5);
        videoAppResponse.setThumbnailUrl("http://stream.com/thumb.png");
        videoAppResponse.setStatus(VideoStatus.CERTIFIED);

        FindArticleAppResponse appResponseNoArticle = new FindArticleAppResponse();
        appResponseNoArticle.setStatus(FindArticleAppResponseStatus.NO_ARTICLE);

        FindArticleAppResponse appResponseSuccess = new FindArticleAppResponse();
        appResponseSuccess.setStatus(FindArticleAppResponseStatus.SUCCESS);
        appResponseSuccess.setId(42L);
        appResponseSuccess.setUser(userAppResponse);
        appResponseSuccess.setVideo(videoAppResponse);
        appResponseSuccess.setTitle("안녕");
        appResponseSuccess.setDescription("세상");
        appResponseSuccess.setLevel(2);
        appResponseSuccess.setAngle(32);
        appResponseSuccess.setViewCount(2342);
        appResponseSuccess.setFavoriteCount(323);
        appResponseSuccess.setRegisterDateTime(this.now);
        appResponseSuccess.setFavorite(true);

        given(this.articleApplication.find(notNull())).willReturn(appResponseNoArticle);
        given(this.articleApplication.find(argThat(argument ->
                argument != null && argument.getArticleId() == 42L))).willReturn(appResponseSuccess);

        ResultActions performInvalidId = this.mockMvc.perform(get("/articles/42f"));
        ResultActions performNoArticle = this.mockMvc.perform(get("/articles/1"));
        ResultActions performOk = this.mockMvc.perform(get("/articles/42"));

        performInvalidId.andExpect(status().isNotFound());
        performNoArticle.andExpect(status().isNotFound());
        performOk.andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.articleId").value(appResponseSuccess.getId().orElseThrow()))
                .andExpect(jsonPath("$.title").value(appResponseSuccess.getTitle()))
                .andExpect(jsonPath("$.description").value(appResponseSuccess.getDescription()))
                .andExpect(jsonPath("$.level").value(appResponseSuccess.getLevel()))
                .andExpect(jsonPath("$.angle").value(appResponseSuccess.getAngle()))
                .andExpect(jsonPath("$.viewCount").value(appResponseSuccess.getViewCount()))
                .andExpect(jsonPath("$.favoriteCount").value(appResponseSuccess.getFavoriteCount()))
                .andExpect(jsonPath("$.registerDateTime").value(appResponseSuccess.getRegisterDateTime()
                        .orElseThrow().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))))
                .andExpect(jsonPath("$.favorite").value(appResponseSuccess.isFavorite()))
                .andExpect(jsonPath("$.user.username").value(userAppResponse.getUsername().orElseThrow()))
                .andExpect(jsonPath("$.user.tier").value(userAppResponse.getTier()))
                .andExpect(jsonPath("$.user.score").value(userAppResponse.getScore() / 100.0))
                .andExpect(jsonPath("$.user.rank").value(userAppResponse.getRank()))
                .andExpect(jsonPath("$.user.percentile").value(userAppResponse.getPercentile()))
                .andExpect(jsonPath("$.user.articleCount").value(userAppResponse.getArticleCount()))
                .andExpect(jsonPath("$.user.articleCertifiedCount").value(userAppResponse.getArticleCertifiedCount()))
                .andExpect(jsonPath("$.user.registerDateTime").value(userAppResponse.getRegisterDateTime()
                        .orElseThrow().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))))
                .andExpect(jsonPath("$.video.videoId").value(videoAppResponse.getUuid()))
                .andExpect(jsonPath("$.video.streamingUrl").value(videoAppResponse.getStreamingUrl()))
                .andExpect(jsonPath("$.video.streamingLength").value(videoAppResponse.getStreamingLength()))
                .andExpect(jsonPath("$.video.streamingAspectRatio").value(videoAppResponse.getStreamingAspectRatio()))
                .andExpect(jsonPath("$.video.thumbnailUrl").value(videoAppResponse.getThumbnailUrl()))
                .andExpect(jsonPath("$.video.status").value(videoAppResponse.getStatus().toString()));
    }

    @Test
    void writeArticle() {
    }

    @WithMockUser(username = "user")
    @Test
    void editArticle() throws Exception {
        EditArticleAppResponse appResponseNoArticle = new EditArticleAppResponse();
        appResponseNoArticle.setStatus(EditArticleAppResponseStatus.NO_ARTICLE);

        EditArticleAppResponse appResponseForbidden = new EditArticleAppResponse();
        appResponseForbidden.setStatus(EditArticleAppResponseStatus.FORBIDDEN);

        EditArticleAppResponse appResponseSuccess = new EditArticleAppResponse();
        appResponseSuccess.setStatus(EditArticleAppResponseStatus.SUCCESS);

        given(this.articleApplication.edit(notNull())).willReturn(appResponseNoArticle);
        given(this.articleApplication.edit(argThat(argument ->
                argument != null && argument.getArticleId() == 41L))).willReturn(appResponseForbidden);
        given(this.articleApplication.edit(argThat(argument ->
                argument != null && argument.getArticleId() == 42L &&
                        argument.getUsername().equals("user")))).willReturn(appResponseSuccess);

        ResultActions performInvalidId =
                this.mockMvc.perform(patch("/articles/42f").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "title": "asdf abgqwaw fdsasdc fd",
                                    "description": "23 asdf c23 3g4 2ba"
                                }"""));
        ResultActions performNoArticle =
                this.mockMvc.perform(patch("/articles/1").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "title": "ab asfd fasa fsc",
                                    "description": "cbf adsfdas fads a"
                                }"""));
        ResultActions performForbidden =
                this.mockMvc.perform(patch("/articles/41").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "title": "gr eagw 422t  42tabc",
                                    "description": "cg4 g 44g2q g4ba"
                                }"""));
        ResultActions performOk =
                this.mockMvc.perform(patch("/articles/42").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "title": "asdf abgqwaw fdsasdc fd",
                                    "description": "23 asdf c23 3g4 2ba"
                                }"""));

        performInvalidId.andExpect(status().isNotFound());
        performNoArticle.andExpect(status().isNotFound());
        performForbidden.andExpect(status().isForbidden());
        performOk.andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.articleId").value("42"))
                .andExpect(jsonPath("$.title").value("asdf abgqwaw fdsasdc fd"))
                .andExpect(jsonPath("$.description").value("23 asdf c23 3g4 2ba"));
    }

    @Test
    void deleteArticle() {
    }

    @Test
    void reactArticle() {
    }

    @Test
    void searchArticle() {
    }
}
