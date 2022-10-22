package kr.njw.gripp.video.controller;

import kr.njw.gripp.video.application.VideoApplication;
import kr.njw.gripp.video.application.dto.FindVideoAppResponse;
import kr.njw.gripp.video.application.dto.UploadVideoAppResponse;
import kr.njw.gripp.video.entity.vo.VideoStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(VideoController.class)
class VideoControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private VideoApplication videoApplication;

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @WithMockUser
    @Test
    void uploadVideo() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test".getBytes());
        MockMultipartFile file2 = new MockMultipartFile("file", "test2".getBytes());
        MockMultipartFile file3 = new MockMultipartFile("file", "test3".getBytes());

        UploadVideoAppResponse appResponse = new UploadVideoAppResponse();
        appResponse.setSuccess(false);
        UploadVideoAppResponse appResponse2 = new UploadVideoAppResponse();
        appResponse2.setSuccess(false);
        appResponse2.setMessage("my error");
        UploadVideoAppResponse appResponse3 = new UploadVideoAppResponse();
        appResponse3.setSuccess(true);
        appResponse3.setUuid("uuid");

        given(this.videoApplication.uploadVideo(file)).willReturn(appResponse);
        given(this.videoApplication.uploadVideo(file2)).willReturn(appResponse2);
        given(this.videoApplication.uploadVideo(file3)).willReturn(appResponse3);

        ResultActions perform = this.mockMvc.perform(multipart("/videos").file(file).with(csrf()));
        ResultActions perform2 = this.mockMvc.perform(multipart("/videos").file(file2).with(csrf()));
        ResultActions perform3 = this.mockMvc.perform(multipart("/videos").file(file3).with(csrf()));

        perform.andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.errors[0]").value("unknown error"));

        perform2.andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.errors[0]").value("my error"));

        perform3.andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.videoId").value(appResponse3.getUuid()));
    }

    @WithMockUser
    @Test
    void findVideo() throws Exception {
        FindVideoAppResponse appResponse = new FindVideoAppResponse();
        appResponse.setSuccess(true);
        appResponse.setUuid("ko");
        appResponse.setStreamingUrl("http://stream.com/movie.mp4");
        appResponse.setStreamingLength(10);
        appResponse.setStreamingAspectRatio(1.5);
        appResponse.setThumbnailUrl("http://stream.com/thumb.png");
        appResponse.setStatus(VideoStatus.CERTIFIED);

        given(this.videoApplication.findVideo(any())).willReturn(new FindVideoAppResponse());
        given(this.videoApplication.findVideo(appResponse.getUuid())).willReturn(appResponse);

        ResultActions perform = this.mockMvc.perform(get("/videos/no"));
        ResultActions perform2 = this.mockMvc.perform(get("/videos/" + appResponse.getUuid()));

        perform.andExpect(status().isNotFound());
        perform2.andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.videoId").value(appResponse.getUuid()))
                .andExpect(jsonPath("$.streamingUrl").value(appResponse.getStreamingUrl()))
                .andExpect(jsonPath("$.streamingLength").value(appResponse.getStreamingLength()))
                .andExpect(jsonPath("$.streamingAspectRatio").value(appResponse.getStreamingAspectRatio()))
                .andExpect(jsonPath("$.thumbnailUrl").value(appResponse.getThumbnailUrl()))
                .andExpect(jsonPath("$.status").value(appResponse.getStatus().toString()));
    }
}
