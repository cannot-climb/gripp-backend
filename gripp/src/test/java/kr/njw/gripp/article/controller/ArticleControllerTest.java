package kr.njw.gripp.article.controller;

import kr.njw.gripp.article.application.ArticleApplication;
import kr.njw.gripp.article.application.dto.*;
import kr.njw.gripp.article.application.dto.search.SearchArticleAppRequestOrder;
import kr.njw.gripp.article.application.dto.search.SearchArticleAppResponse;
import kr.njw.gripp.article.application.dto.search.SearchArticleAppResponseItem;
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
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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

    @WithMockUser(username = "user")
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

        given(this.articleApplication.find(any())).willReturn(appResponseNoArticle);
        given(this.articleApplication.find(argThat(argument ->
                argument != null && argument.getUsernameRequestedBy().equals("user") &&
                        argument.getArticleId() == 42L))).willReturn(appResponseSuccess);

        ResultActions performInvalidId = this.mockMvc.perform(get("/articles/42f"));
        ResultActions performNoArticle = this.mockMvc.perform(get("/articles/1"));
        ResultActions performOk = this.mockMvc.perform(get("/articles/42"));

        performInvalidId.andExpect(status().isNotFound());
        performNoArticle.andExpect(status().isNotFound());
        performOk.andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.articleId").value(is(appResponseSuccess.getId().orElseThrow().toString())))
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

    @WithMockUser(username = "user")
    @Test
    void writeArticle() throws Exception {
        WriteArticleAppResponse appResponseInvalidUser = new WriteArticleAppResponse();
        appResponseInvalidUser.setStatus(WriteArticleAppResponseStatus.NO_USER);

        WriteArticleAppResponse appResponseInvalidVideo = new WriteArticleAppResponse();
        appResponseInvalidVideo.setStatus(WriteArticleAppResponseStatus.NO_VIDEO);

        WriteArticleAppResponse appResponseAlreadyPostedVideo = new WriteArticleAppResponse();
        appResponseAlreadyPostedVideo.setStatus(WriteArticleAppResponseStatus.ALREADY_POSTED_VIDEO);

        WriteArticleAppResponse appResponseSuccess = new WriteArticleAppResponse();
        appResponseSuccess.setStatus(WriteArticleAppResponseStatus.SUCCESS);
        appResponseSuccess.setId(42L);

        given(this.articleApplication.write(any())).willReturn(appResponseInvalidUser);
        given(this.articleApplication.write(
                argThat(argument -> argument != null && argument.getUsername().equals("user"))))
                .willReturn(appResponseInvalidVideo);
        given(this.articleApplication.write(
                argThat(argument -> argument != null && argument.getUsername().equals("user") &&
                        argument.getVideoUuid().equals("hello"))))
                .willReturn(appResponseAlreadyPostedVideo);
        given(this.articleApplication.write(
                argThat(argument -> argument != null && argument.getUsername().equals("user") &&
                        argument.getVideoUuid().equals("uuid"))))
                .willReturn(appResponseSuccess);

        ResultActions performInvalidUser =
                this.mockMvc.perform(post("/articles").with(user("hi")).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "videoId": "1",
                                    "title": "asdf abgqwaw fdsasdc fd",
                                    "description": "23 asdf c23 3g4 2ba",
                                    "level": 15,
                                    "angle": 45
                                }"""));

        ResultActions performInvalidVideo =
                this.mockMvc.perform(post("/articles").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "videoId": "abc",
                                    "title": "asdf abgqwaw fdsasdc fd",
                                    "description": "23 asdf c23 3g4 2ba",
                                    "level": 15,
                                    "angle": 45
                                }"""));

        ResultActions performAlreadyPostedVideo =
                this.mockMvc.perform(post("/articles").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "videoId": "hello",
                                    "title": "asdf abgqwaw fdsasdc fd",
                                    "description": "23 asdf c23 3g4 2ba",
                                    "level": 15,
                                    "angle": 45
                                }"""));

        ResultActions performOk =
                this.mockMvc.perform(post("/articles").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "videoId": "uuid",
                                    "title": "asdf abgqwaw fdsasdc fd",
                                    "description": "23 asdf c23 3g4 2ba",
                                    "level": 15,
                                    "angle": 45
                                }"""));

        then(this.articleApplication).should(times(4)).write(argThat(argument ->
                argument.getTitle().equals("asdf abgqwaw fdsasdc fd") &&
                        argument.getDescription().equals("23 asdf c23 3g4 2ba") &&
                        argument.getLevel() == 15 && argument.getAngle() == 45));
        then(this.articleApplication).should(times(1)).write(argThat(argument ->
                argument.getUsername().equals("user") && argument.getVideoUuid().equals("uuid") &&
                        argument.getTitle().equals("asdf abgqwaw fdsasdc fd") &&
                        argument.getDescription().equals("23 asdf c23 3g4 2ba") &&
                        argument.getLevel() == 15 && argument.getAngle() == 45));

        performInvalidUser.andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.errors[0]").value("invalid user"));
        performInvalidVideo.andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.errors[0]").value("invalid video"));
        performAlreadyPostedVideo.andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.errors[0]").value("already posted video"));
        performOk.andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.articleId").value(is(appResponseSuccess.getId().orElseThrow().toString())));
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

        given(this.articleApplication.edit(any())).willReturn(appResponseNoArticle);
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

        then(this.articleApplication).should(times(1)).edit(argThat(argument ->
                argument.getUsername().equals("user") &&
                        argument.getTitle().equals("asdf abgqwaw fdsasdc fd") &&
                        argument.getDescription().equals("23 asdf c23 3g4 2ba")));

        performInvalidId.andExpect(status().isNotFound());
        performNoArticle.andExpect(status().isNotFound());
        performForbidden.andExpect(status().isForbidden());
        performOk.andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.articleId").value(is("42")))
                .andExpect(jsonPath("$.title").value("asdf abgqwaw fdsasdc fd"))
                .andExpect(jsonPath("$.description").value("23 asdf c23 3g4 2ba"));
    }

    @WithMockUser(username = "user")
    @Test
    void deleteArticle() throws Exception {
        DeleteArticleAppResponse appResponseNoArticle = new DeleteArticleAppResponse();
        appResponseNoArticle.setStatus(DeleteArticleAppResponseStatus.NO_ARTICLE);

        DeleteArticleAppResponse appResponseForbidden = new DeleteArticleAppResponse();
        appResponseForbidden.setStatus(DeleteArticleAppResponseStatus.FORBIDDEN);

        DeleteArticleAppResponse appResponseSuccess = new DeleteArticleAppResponse();
        appResponseSuccess.setStatus(DeleteArticleAppResponseStatus.SUCCESS);

        given(this.articleApplication.delete(any())).willReturn(appResponseNoArticle);
        given(this.articleApplication.delete(argThat(argument ->
                argument != null && argument.getArticleId() == 41L))).willReturn(appResponseForbidden);
        given(this.articleApplication.delete(argThat(argument ->
                argument != null && argument.getArticleId() == 42L &&
                        argument.getUsername().equals("user")))).willReturn(appResponseSuccess);

        ResultActions performInvalidId = this.mockMvc.perform(delete("/articles/42f").with(csrf()));
        ResultActions performNoArticle = this.mockMvc.perform(delete("/articles/1").with(csrf()));
        ResultActions performForbidden = this.mockMvc.perform(delete("/articles/41").with(csrf()));
        ResultActions performOk = this.mockMvc.perform(delete("/articles/42").with(csrf()));

        performInvalidId.andExpect(status().isNotFound());
        performNoArticle.andExpect(status().isNotFound());
        performForbidden.andExpect(status().isForbidden());
        performOk.andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.articleId").value(is("42")));
    }

    @WithMockUser(username = "user")
    @Test
    void reactArticle() throws Exception {
        ReactArticleAppResponse appResponseNoArticle = new ReactArticleAppResponse();
        appResponseNoArticle.setStatus(ReactArticleAppResponseStatus.NO_ARTICLE);

        ReactArticleAppResponse appResponseSuccessToFavorite = new ReactArticleAppResponse();
        appResponseSuccessToFavorite.setStatus(ReactArticleAppResponseStatus.SUCCESS);
        appResponseSuccessToFavorite.setFavorite(true);

        ReactArticleAppResponse appResponseSuccessToCancelFavorite = new ReactArticleAppResponse();
        appResponseSuccessToCancelFavorite.setStatus(ReactArticleAppResponseStatus.SUCCESS);
        appResponseSuccessToCancelFavorite.setFavorite(false);

        given(this.articleApplication.react(any())).willReturn(appResponseNoArticle);
        given(this.articleApplication.react(argThat(argument ->
                argument != null && argument.getArticleId() == 42L &&
                        argument.getUsernameRequestedBy().equals("user") &&
                        argument.isFavorite()))).willReturn(appResponseSuccessToFavorite);
        given(this.articleApplication.react(argThat(argument ->
                argument != null && argument.getArticleId() == 42L &&
                        argument.getUsernameRequestedBy().equals("user") &&
                        !argument.isFavorite()))).willReturn(appResponseSuccessToCancelFavorite);

        ResultActions performInvalidId = this.mockMvc.perform(
                patch("/articles/42f/reaction").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "favorite": true
                                }"""));
        ResultActions performNoArticle = this.mockMvc.perform(
                patch("/articles/1/reaction").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "favorite": true
                                }"""));
        ResultActions performOkToFavorite = this.mockMvc.perform(
                patch("/articles/42/reaction").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "favorite": true
                                }"""));
        ResultActions performOkToCancelFavorite = this.mockMvc.perform(
                patch("/articles/42/reaction").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "favorite": false
                                }"""));

        performInvalidId.andExpect(status().isNotFound());
        performNoArticle.andExpect(status().isNotFound());
        performOkToFavorite.andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.favorite").value(is(true)));
        performOkToCancelFavorite.andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.favorite").value(is(false)));
    }

    @WithMockUser
    @Test
    void searchArticles() throws Exception {
        FindVideoAppResponse videoAppResponse = new FindVideoAppResponse();
        videoAppResponse.setSuccess(true);
        videoAppResponse.setUuid("ko");
        videoAppResponse.setStreamingUrl("http://stream.com/movie.mp4");
        videoAppResponse.setStreamingLength(10);
        videoAppResponse.setStreamingAspectRatio(1.5);
        videoAppResponse.setThumbnailUrl("http://stream.com/thumb.png");
        videoAppResponse.setStatus(VideoStatus.CERTIFIED);

        SearchArticleAppResponseItem appResponseItem = new SearchArticleAppResponseItem();
        appResponseItem.setId(42L);
        appResponseItem.setUsername("admin");
        appResponseItem.setVideo(videoAppResponse);
        appResponseItem.setTitle("스프링");
        appResponseItem.setDescription("부트");
        appResponseItem.setLevel(11);
        appResponseItem.setAngle(5);
        appResponseItem.setViewCount(22);
        appResponseItem.setFavoriteCount(1);
        appResponseItem.setRegisterDateTime(this.now);

        SearchArticleAppResponse appResponse = new SearchArticleAppResponse();
        appResponse.setArticles(List.of(appResponseItem, appResponseItem));
        appResponse.setNextPageToken("asdf");

        given(this.articleApplication.search(any())).willReturn(appResponse);

        ResultActions performDefault = this.mockMvc.perform(
                post("/articles/search").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                }"""));

        ResultActions performComplex = this.mockMvc.perform(
                post("/articles/search").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "filters": [
                                        {
                                            "type": "TITLE",
                                            "titleLike": "the wall"
                                        },
                                        {
                                            "type": "USER",
                                            "username": "njw1204"
                                        },
                                        {
                                            "type": "LEVEL",
                                            "minLevel": 1,
                                            "maxLevel": 19
                                        },
                                        {
                                            "type": "ANGLE",
                                            "minAngle": 2,
                                            "maxAngle": 70
                                        },
                                        {
                                            "type": "DATETIME",
                                            "minDateTime": "2022-09-17 11:37:09",
                                            "maxDateTime": "2023-12-04 01:05:59"
                                        },
                                        {
                                            "type": "STATUS",
                                            "statusIn": [
                                                "NO_CERTIFIED",
                                                "PREPROCESSING",
                                                "CERTIFIED"
                                            ]
                                        },
                                        {
                                            "type": "STATUS",
                                            "statusIn": []
                                        }
                                    ],
                                    "order": "HARD",
                                    "pageToken": "NbVLWuyLVpF6zeu4fX-m7aO66lMeoim_01v9LdB"
                                }"""));

        performDefault.andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.articles").value(hasSize(2)))
                .andExpect(jsonPath("$.articles[*].articleId").value(
                        everyItem(is(appResponseItem.getId().orElseThrow().toString()))))
                .andExpect(jsonPath("$.articles[*].username").value(
                        everyItem(is(appResponseItem.getUsername()))))
                .andExpect(jsonPath("$.articles[*].title").value(
                        everyItem(is(appResponseItem.getTitle()))))
                .andExpect(jsonPath("$.articles[*].description").value(
                        everyItem(is(appResponseItem.getDescription()))))
                .andExpect(jsonPath("$.articles[*].level").value(
                        everyItem(is(appResponseItem.getLevel()))))
                .andExpect(jsonPath("$.articles[*].angle").value(
                        everyItem(is(appResponseItem.getAngle()))))
                .andExpect(jsonPath("$.articles[*].viewCount").value(
                        everyItem(is((int) appResponseItem.getViewCount()))))
                .andExpect(jsonPath("$.articles[*].favoriteCount").value(
                        everyItem(is((int) appResponseItem.getFavoriteCount()))))
                .andExpect(jsonPath("$.articles[*].registerDateTime").value(
                        everyItem(is(appResponseItem.getRegisterDateTime().orElseThrow()
                                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))))))
                .andExpect(jsonPath("$.articles[*].video.videoId").value(everyItem(is(videoAppResponse.getUuid()))))
                .andExpect(jsonPath("$.articles[*].video.streamingUrl").value(
                        everyItem(is(videoAppResponse.getStreamingUrl()))))
                .andExpect(jsonPath("$.articles[*].video.streamingLength").value(
                        everyItem(is(videoAppResponse.getStreamingLength()))))
                .andExpect(jsonPath("$.articles[*].video.streamingAspectRatio").value(
                        everyItem(is(videoAppResponse.getStreamingAspectRatio()))))
                .andExpect(jsonPath("$.articles[*].video.thumbnailUrl").value(
                        everyItem(is(videoAppResponse.getThumbnailUrl()))))
                .andExpect(jsonPath("$.articles[*].video.status").value(
                        everyItem(is(videoAppResponse.getStatus().toString()))))
                .andExpect(jsonPath("$.nextPageToken").value(is(appResponse.getNextPageToken())));

        performComplex.andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().string(performDefault.andReturn().getResponse().getContentAsString()));
    }

    @WithMockUser
    @Test
    void searchArticlesEmpty() throws Exception {
        SearchArticleAppResponse appResponseEmpty = new SearchArticleAppResponse();

        given(this.articleApplication.search(any())).willReturn(appResponseEmpty);

        ResultActions performDefault = this.mockMvc.perform(
                post("/articles/search").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                }"""));

        ResultActions performOrderByView = this.mockMvc.perform(
                post("/articles/search").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "order": "VIEW"
                                }"""));

        ResultActions performWithPageToken = this.mockMvc.perform(
                post("/articles/search").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "pageToken": "pp"
                                }"""));

        ResultActions performOrderByNewWithPageToken = this.mockMvc.perform(
                post("/articles/search").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "order": "NEW",
                                    "pageToken": "pp"
                                }"""));

        ResultActions performEmptyFiltersOrderByEasy = this.mockMvc.perform(
                post("/articles/search").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "filters": [],
                                    "order": "EASY"
                                }"""));

        ResultActions performEmptyFiltersOrderByOldWithPageToken = this.mockMvc.perform(
                post("/articles/search").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "filters": [],
                                    "order": "OLD",
                                    "pageToken": "pp"
                                }"""));

        ResultActions performTitleFilterOrderByPopularWithPageToken = this.mockMvc.perform(
                post("/articles/search").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "filters": [
                                        {
                                            "type": "TITLE",
                                            "titleLike": "hello"
                                        }
                                    ],
                                    "order": "POPULAR",
                                    "pageToken": "pp"
                                }"""));

        ResultActions performComplex = this.mockMvc.perform(
                post("/articles/search").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "filters": [
                                        {
                                            "type": "TITLE",
                                            "titleLike": "the wall"
                                        },
                                        {
                                            "type": "USER",
                                            "username": "njw1204"
                                        },
                                        {
                                            "type": "LEVEL",
                                            "minLevel": 1,
                                            "maxLevel": 19
                                        },
                                        {
                                            "type": "ANGLE",
                                            "minAngle": 2,
                                            "maxAngle": 70
                                        },
                                        {
                                            "type": "DATETIME",
                                            "minDateTime": "2022-09-17 11:37:09",
                                            "maxDateTime": "2023-12-04 01:05:59"
                                        },
                                        {
                                            "type": "STATUS",
                                            "statusIn": [
                                                "NO_CERTIFIED",
                                                "PREPROCESSING",
                                                "CERTIFIED"
                                            ]
                                        },
                                        {
                                            "type": "STATUS",
                                            "statusIn": []
                                        }
                                    ],
                                    "order": "HARD",
                                    "pageToken": "NbVLWuyLVpF6zeu4fX-m7aO66lMeoim_01v9LdB"
                                }"""));

        then(this.articleApplication).should(times(8)).search(any());

        then(this.articleApplication).should(times(6)).search(argThat(argument -> argument.getFilters().isEmpty()));
        then(this.articleApplication).should(times(1))
                .search(argThat(argument -> argument.getFilters().size() == 1 &&
                        argument.getFilters().get(0).getTitleLike().equals("hello")));
        then(this.articleApplication).should(times(1))
                .search(argThat(argument -> argument.getFilters().size() == 7 &&
                        argument.getFilters().get(0).getTitleLike().equals("the wall") &&
                        argument.getFilters().get(1).getUsername().equals("njw1204") &&
                        argument.getFilters().get(2).getMinLevel() == 1 &&
                        argument.getFilters().get(2).getMaxLevel() == 19 &&
                        argument.getFilters().get(3).getMinAngle() == 2 &&
                        argument.getFilters().get(3).getMaxAngle() == 70 &&
                        argument.getFilters()
                                .get(4)
                                .getMinDateTime()
                                .equals(LocalDateTime.of(2022, 9, 17, 11, 37, 9)) &&
                        argument.getFilters()
                                .get(4)
                                .getMaxDateTime()
                                .equals(LocalDateTime.of(2023, 12, 4, 1, 5, 59)) &&
                        argument.getFilters().get(5).getStatusIn().size() == 3 &&
                        argument.getFilters().get(5).getStatusIn().get(0) == VideoStatus.NO_CERTIFIED &&
                        argument.getFilters().get(5).getStatusIn().get(1) == VideoStatus.PREPROCESSING &&
                        argument.getFilters().get(5).getStatusIn().get(2) == VideoStatus.CERTIFIED &&
                        argument.getFilters().get(6).getStatusIn().size() == 0));

        then(this.articleApplication).should(times(3))
                .search(argThat(argument -> argument.getOrder() == SearchArticleAppRequestOrder.NEW));
        then(this.articleApplication).should(times(1))
                .search(argThat(argument -> argument.getOrder() == SearchArticleAppRequestOrder.OLD));
        then(this.articleApplication).should(times(1))
                .search(argThat(argument -> argument.getOrder() == SearchArticleAppRequestOrder.VIEW));
        then(this.articleApplication).should(times(1))
                .search(argThat(argument -> argument.getOrder() == SearchArticleAppRequestOrder.POPULAR));
        then(this.articleApplication).should(times(1))
                .search(argThat(argument -> argument.getOrder() == SearchArticleAppRequestOrder.HARD));
        then(this.articleApplication).should(times(1))
                .search(argThat(argument -> argument.getOrder() == SearchArticleAppRequestOrder.EASY));

        then(this.articleApplication).should(times(3))
                .search(argThat(argument -> argument.getPageToken().equals("")));
        then(this.articleApplication).should(times(4))
                .search(argThat(argument -> argument.getPageToken().equals("pp")));
        then(this.articleApplication).should(times(1))
                .search(argThat(argument -> argument.getPageToken().equals("NbVLWuyLVpF6zeu4fX-m7aO66lMeoim_01v9LdB")));

        performDefault.andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.articles").value(hasSize(0)))
                .andExpect(jsonPath("$.nextPageToken").value(is("")));

        performOrderByView.andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.articles").value(hasSize(0)))
                .andExpect(jsonPath("$.nextPageToken").value(is("")));

        performWithPageToken.andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.articles").value(hasSize(0)))
                .andExpect(jsonPath("$.nextPageToken").value(is("")));

        performOrderByNewWithPageToken.andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.articles").value(hasSize(0)))
                .andExpect(jsonPath("$.nextPageToken").value(is("")));

        performEmptyFiltersOrderByEasy.andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.articles").value(hasSize(0)))
                .andExpect(jsonPath("$.nextPageToken").value(is("")));

        performEmptyFiltersOrderByOldWithPageToken.andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.articles").value(hasSize(0)))
                .andExpect(jsonPath("$.nextPageToken").value(is("")));

        performTitleFilterOrderByPopularWithPageToken.andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.articles").value(hasSize(0)))
                .andExpect(jsonPath("$.nextPageToken").value(is("")));

        performComplex.andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.articles").value(hasSize(0)))
                .andExpect(jsonPath("$.nextPageToken").value(is("")));
    }
}
