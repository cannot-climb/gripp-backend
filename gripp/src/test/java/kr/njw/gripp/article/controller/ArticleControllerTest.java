package kr.njw.gripp.article.controller;

import kr.njw.gripp.article.application.ArticleApplication;
import kr.njw.gripp.article.application.dto.EditArticleAppResponse;
import kr.njw.gripp.article.application.dto.EditArticleAppResponseStatus;
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

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ArticleController.class)
class ArticleControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private ArticleApplication articleApplication;

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void findArticle() {
    }

    @Test
    void writeArticle() {
    }

    @WithMockUser(username = "user")
    @Test
    void editArticle() throws Exception {
        EditArticleAppResponse appResponse = new EditArticleAppResponse();
        appResponse.setStatus(EditArticleAppResponseStatus.NO_ARTICLE);

        EditArticleAppResponse appResponse2 = new EditArticleAppResponse();
        appResponse2.setStatus(EditArticleAppResponseStatus.FORBIDDEN);

        EditArticleAppResponse appResponse3 = new EditArticleAppResponse();
        appResponse3.setStatus(EditArticleAppResponseStatus.SUCCESS);

        given(this.articleApplication.edit(notNull())).willReturn(appResponse);
        given(this.articleApplication.edit(argThat(argument ->
                argument != null && argument.getArticleId() == 41L))).willReturn(appResponse2);
        given(this.articleApplication.edit(argThat(argument ->
                argument != null && argument.getArticleId() == 42L &&
                        argument.getUsername().equals("user")))).willReturn(appResponse3);

        ResultActions perform =
                this.mockMvc.perform(patch("/articles/1").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "title": "ab asfd fasa fsc",
                                    "description": "cbf adsfdas fads a"
                                }"""));
        ResultActions perform2 =
                this.mockMvc.perform(patch("/articles/41").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "title": "gr eagw 422t  42tabc",
                                    "description": "cg4 g 44g2q g4ba"
                                }"""));
        ResultActions perform3 =
                this.mockMvc.perform(patch("/articles/42").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "title": "asdf abgqwaw fdsasdc fd",
                                    "description": "23 asdf c23 3g4 2ba"
                                }"""));
        ResultActions perform4 =
                this.mockMvc.perform(patch("/articles/42f").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "title": "asdf abgqwaw fdsasdc fd",
                                    "description": "23 asdf c23 3g4 2ba"
                                }"""));

        perform.andExpect(status().isNotFound());
        perform2.andExpect(status().isForbidden());
        perform3.andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.articleId").value("42"))
                .andExpect(jsonPath("$.title").value("asdf abgqwaw fdsasdc fd"))
                .andExpect(jsonPath("$.description").value("23 asdf c23 3g4 2ba"));
        perform4.andExpect(status().isNotFound());
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
