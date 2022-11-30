package kr.njw.gripp.video.application;

import kr.njw.gripp.article.entity.Article;
import kr.njw.gripp.article.repository.ArticleRepository;
import kr.njw.gripp.global.config.RabbitConfig;
import kr.njw.gripp.user.entity.User;
import kr.njw.gripp.user.repository.UserRepository;
import kr.njw.gripp.user.service.UserService;
import kr.njw.gripp.video.application.dto.FindVideoAppResponse;
import kr.njw.gripp.video.application.dto.UploadVideoAppResponse;
import kr.njw.gripp.video.entity.Video;
import kr.njw.gripp.video.entity.vo.VideoStatus;
import kr.njw.gripp.video.repository.VideoRepository;
import org.apache.commons.io.FilenameUtils;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.XMPDM;
import org.apache.tika.parser.mp4.MP4Parser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class VideoApplicationImplTest {
    @InjectMocks
    private VideoApplicationImpl videoApplicationImpl;
    @Mock
    private UserService userService;
    @Mock
    private ArticleRepository articleRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private VideoRepository videoRepository;
    @Mock
    private AmqpTemplate amqpTemplate;
    @Mock
    private Tika tika;
    @Mock
    private MP4Parser mp4Parser;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        this.now = LocalDateTime.now();
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void uploadVideo() throws IOException, TikaException, SAXException {
        MockMultipartFile file = spy(new MockMultipartFile("movie2.mp4", "movie.mp4", null, "movie".getBytes()));

        given(this.tika.detect(any(InputStream.class))).willReturn("video/mp4");
        willAnswer(invocation -> {
            Metadata metadata = invocation.getArgument(2);
            metadata.set(XMPDM.DURATION, "5.0");
            return null;
        }).given(this.mp4Parser).parse(any(), any(), any(), any());
        willDoNothing().given(file).transferTo(any(Path.class));

        UploadVideoAppResponse response = this.videoApplicationImpl.uploadVideo(file);

        then(this.videoRepository).should(times(1))
                .save(argThat(video -> video.getStatus() == VideoStatus.PREPROCESSING &&
                        video.getOriginalFileName().equals(file.getOriginalFilename()) &&
                        video.getOriginalFileExtension()
                                .equals(FilenameUtils.getExtension(file.getOriginalFilename()))));
        then(file).should(times(1)).transferTo(any(Path.class));
        then(this.amqpTemplate).should(times(1))
                .convertAndSend(RabbitConfig.VIDEO_PROCESSOR_QUEUE_KEY,
                        new VideoApplicationImpl.VideoProcessorRequest(response.getUuid(),
                                response.getUuid() + "." + FilenameUtils.getExtension(file.getOriginalFilename())));

        assertThat(response.isSuccess()).isTrue();
        assertThat(UUID.fromString(response.getUuid())).isNotNull();
    }

    @Test
    void uploadVideoWithInvalidFile() throws IOException {
        MockMultipartFile file = new MockMultipartFile("test.mp4", "test.exe", null, "test".getBytes());
        MockMultipartFile file2 = new MockMultipartFile("test.mp4", "test.mp4", null, "test".getBytes());

        given(this.tika.detect(any(InputStream.class))).willReturn(MediaType.APPLICATION_OCTET_STREAM_VALUE);

        UploadVideoAppResponse response = this.videoApplicationImpl.uploadVideo(file);
        UploadVideoAppResponse response2 = this.videoApplicationImpl.uploadVideo(file2);

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo("invalid file extension");

        assertThat(response2.isSuccess()).isFalse();
        assertThat(response2.getMessage()).isEqualTo("invalid video mime type");
    }

    @Test
    void uploadVideoWithInvalidMetadata() throws IOException {
        MockMultipartFile file = new MockMultipartFile("test.mov", "test.mov", null, "test".getBytes());

        given(this.tika.detect(any(InputStream.class))).willReturn("video/quicktime");

        UploadVideoAppResponse response = this.videoApplicationImpl.uploadVideo(file);

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo("fail to calculate duration");
    }

    @Test
    void uploadVideoWithInvalidDuration() throws IOException, TikaException, SAXException {
        MockMultipartFile file = new MockMultipartFile("test.mp4", "test.mp4", null, "test".getBytes());

        given(this.tika.detect(any(InputStream.class))).willReturn("video/mp4");
        willAnswer(invocation -> {
            Metadata metadata = invocation.getArgument(2);
            metadata.set(XMPDM.DURATION, "0.9");
            return null;
        }).given(this.mp4Parser).parse(any(), any(), any(), any());

        UploadVideoAppResponse response = this.videoApplicationImpl.uploadVideo(file);

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo("too short video");
    }

    @Test
    void uploadVideoWithTikaException() throws IOException, TikaException, SAXException {
        MockMultipartFile file = new MockMultipartFile("video.mp4", "video.mp4", null, "video".getBytes());

        given(this.tika.detect(any(InputStream.class))).willReturn("video/mp4");
        willThrow(new TikaException("")).given(this.mp4Parser).parse(any(), any(), any(), any());

        Throwable throwable = catchThrowable(() -> this.videoApplicationImpl.uploadVideo(file));

        assertThat(throwable).isInstanceOf(IOException.class);
    }

    @Test
    void uploadVideoWithSAXException() throws IOException, TikaException, SAXException {
        MockMultipartFile file = new MockMultipartFile("video.mp4", "video.mov", null, "video".getBytes());

        given(this.tika.detect(any(InputStream.class))).willReturn("video/quicktime");
        willThrow(new SAXException("")).given(this.mp4Parser).parse(any(), any(), any(), any());

        Throwable throwable = catchThrowable(() -> this.videoApplicationImpl.uploadVideo(file));

        assertThat(throwable).isInstanceOf(IOException.class);
    }

    @Test
    void findVideo() {
        Video video = Video.builder()
                .id(1L)
                .uuid("ko")
                .streamingUrl("http://stream.com/movie.mp4")
                .streamingLength(10)
                .streamingAspectRatio(1.5)
                .thumbnailUrl("http://stream.com/thumb.png")
                .originalFileName("test.mp4")
                .originalFileExtension("mp4")
                .status(VideoStatus.CERTIFIED)
                .registerDateTime(this.now)
                .build();

        given(this.videoRepository.findByUuid(any())).willReturn(Optional.empty());
        given(this.videoRepository.findByUuid(video.getUuid())).willReturn(Optional.of(video));

        FindVideoAppResponse response = this.videoApplicationImpl.findVideo("uuid");
        FindVideoAppResponse response2 = this.videoApplicationImpl.findVideo(video.getUuid());

        assertThat(response.isSuccess()).isFalse();

        assertThat(response2.isSuccess()).isTrue();
        assertThat(response2.getUuid()).isEqualTo(video.getUuid());
        assertThat(response2.getStreamingUrl()).isEqualTo(video.getStreamingUrl());
        assertThat(response2.getStreamingLength()).isEqualTo(video.getStreamingLength());
        assertThat(response2.getStreamingAspectRatio()).isEqualTo(video.getStreamingAspectRatio());
        assertThat(response2.getThumbnailUrl()).isEqualTo(video.getThumbnailUrl());
        assertThat(response2.getStatus()).isEqualTo(video.getStatus());
    }

    @Test
    void onReturnVideoProcessor() throws IOException {
        Video video = spy(Video.builder()
                .id(1L)
                .uuid("7dc53df5-703e-49b3-8670-b1c468f47f1f")
                .streamingUrl("")
                .streamingLength(0)
                .streamingAspectRatio(0)
                .thumbnailUrl("")
                .originalFileName("no")
                .originalFileExtension("mp4")
                .status(VideoStatus.PREPROCESSING)
                .registerDateTime(this.now)
                .build());

        VideoApplicationImpl.VideoProcessorResponse videoProcessorResponse =
                new VideoApplicationImpl.VideoProcessorResponse();
        videoProcessorResponse.setRequest(new VideoApplicationImpl.VideoProcessorRequest("uuid", "fileName"));

        VideoApplicationImpl.VideoProcessorResponse videoProcessorResponse2 =
                new VideoApplicationImpl.VideoProcessorResponse();
        videoProcessorResponse2.setStreamingUrl(
                "https://objectstorage.ap-seoul-1.oraclecloud.com/n/cngzlmggdnp2/b/gripp/o/sample/master.m3u8");
        videoProcessorResponse2.setStreamingLength(45);
        videoProcessorResponse2.setStreamingAspectRatio(720.0 / 1080.0);
        videoProcessorResponse2.setThumbnailUrl(
                "https://objectstorage.ap-seoul-1.oraclecloud.com/n/cngzlmggdnp2/b/gripp/o/sample/thumbnail.png");
        videoProcessorResponse2.setCertified(false);
        videoProcessorResponse2.setRequest(
                new VideoApplicationImpl.VideoProcessorRequest("7dc53df5-703e-49b3-8670-b1c468f47f1f", "no"));

        given(this.videoRepository.findForUpdateByUuid(any())).willReturn(Optional.empty());
        given(this.videoRepository.findForUpdateByUuid(videoProcessorResponse2.getRequest().getUuid())).willReturn(
                Optional.of(video));

        Throwable throwable = catchThrowable(() ->
                this.videoApplicationImpl.onReturnVideoProcessor(videoProcessorResponse));
        this.videoApplicationImpl.onReturnVideoProcessor(videoProcessorResponse2);

        then(video).should(times(1))
                .startStreaming(videoProcessorResponse2.getStreamingUrl(), videoProcessorResponse2.getStreamingLength(),
                        videoProcessorResponse2.getStreamingAspectRatio(), videoProcessorResponse2.getThumbnailUrl(),
                        videoProcessorResponse2.isCertified());
        then(this.videoRepository).should(times(1)).save(video);

        assertThat(throwable).isInstanceOf(RuntimeException.class);
        assertThat(throwable.getMessage()).startsWith("영상이 존재하지 않습니다");
    }

    @Test
    void onReturnVideoProcessorWithArticle() throws IOException {
        User user = User.builder().id(83L).build();

        Video video = spy(Video.builder().id(2L).build());
        Video video2 = spy(Video.builder().id(22L).build());
        Video video3 = spy(Video.builder().id(222L).build());

        Article article = Article.builder().build();
        Article article2 = Article.builder().user(user).build();

        VideoApplicationImpl.VideoProcessorResponse videoProcessorResponse =
                new VideoApplicationImpl.VideoProcessorResponse();
        videoProcessorResponse.setStreamingUrl(
                "https://objectstorage.ap-seoul-1.oraclecloud.com/n/cngzlmggdnp2/b/gripp/o/sample/master1.m3u8");
        videoProcessorResponse.setStreamingLength(23);
        videoProcessorResponse.setStreamingAspectRatio(22720.0 / 10830.0);
        videoProcessorResponse.setThumbnailUrl(
                "https://objectstorage.ap-seoul-1.oraclecloud.com/n/cngzlmggdnp2/b/gripp/o/sample/thumbnail.png");
        videoProcessorResponse.setCertified(true);
        videoProcessorResponse.setRequest(new VideoApplicationImpl.VideoProcessorRequest("ok", "ok"));

        VideoApplicationImpl.VideoProcessorResponse videoProcessorResponse2 =
                new VideoApplicationImpl.VideoProcessorResponse();
        videoProcessorResponse2.setStreamingUrl(
                "https://objectstorage.ap-seoul-1.oraclecloud.com/n/cngzlmggdnp2/b/gripp/o/sample/master2.m3u8");
        videoProcessorResponse2.setStreamingLength(452);
        videoProcessorResponse2.setStreamingAspectRatio(720.0 / 10830.0);
        videoProcessorResponse2.setThumbnailUrl(
                "https://objectstorage.ap-seoul-1.oraclecloud.com/n/cngzlmggdnp2/b/gripp/o/sample/thumbnail2.png");
        videoProcessorResponse2.setCertified(true);
        videoProcessorResponse2.setRequest(new VideoApplicationImpl.VideoProcessorRequest("ok2", "ok"));

        VideoApplicationImpl.VideoProcessorResponse videoProcessorResponse3 =
                new VideoApplicationImpl.VideoProcessorResponse();
        videoProcessorResponse3.setStreamingUrl(
                "https://objectstorage.ap-seoul-1.oraclecloud.com/n/cngzlmggdnp2/b/gripp/o/sample/master3.m3u8");
        videoProcessorResponse3.setStreamingLength(5);
        videoProcessorResponse3.setStreamingAspectRatio(20.0 / 1080.0);
        videoProcessorResponse3.setThumbnailUrl(
                "https://objectstorage.ap-seoul-1.oraclecloud.com/n/cngzlmggdnp2/b/gripp/o/sample/thumbnail.png");
        videoProcessorResponse3.setCertified(true);
        videoProcessorResponse3.setRequest(new VideoApplicationImpl.VideoProcessorRequest("ok3", "ok"));

        given(this.userRepository.findForUpdateById(user.getId())).willReturn(Optional.of(user));

        given(this.videoRepository.findForUpdateByUuid(videoProcessorResponse.getRequest().getUuid())).willReturn(
                Optional.of(video));
        given(this.videoRepository.findForUpdateByUuid(videoProcessorResponse2.getRequest().getUuid())).willReturn(
                Optional.of(video2));
        given(this.videoRepository.findForUpdateByUuid(videoProcessorResponse3.getRequest().getUuid())).willReturn(
                Optional.of(video3));

        given(this.articleRepository.findForShareByVideoId(video.getId())).willReturn(Optional.empty());
        given(this.articleRepository.findForShareByVideoId(video2.getId())).willReturn(Optional.of(article));
        given(this.articleRepository.findForShareByVideoId(video3.getId())).willReturn(Optional.of(article2));

        this.videoApplicationImpl.onReturnVideoProcessor(videoProcessorResponse);
        this.videoApplicationImpl.onReturnVideoProcessor(videoProcessorResponse2);
        this.videoApplicationImpl.onReturnVideoProcessor(videoProcessorResponse3);

        then(video).should(times(1))
                .startStreaming(videoProcessorResponse.getStreamingUrl(), videoProcessorResponse.getStreamingLength(),
                        videoProcessorResponse.getStreamingAspectRatio(), videoProcessorResponse.getThumbnailUrl(),
                        videoProcessorResponse.isCertified());
        then(video2).should(times(1))
                .startStreaming(videoProcessorResponse2.getStreamingUrl(), videoProcessorResponse2.getStreamingLength(),
                        videoProcessorResponse2.getStreamingAspectRatio(), videoProcessorResponse2.getThumbnailUrl(),
                        videoProcessorResponse2.isCertified());
        then(video3).should(times(1))
                .startStreaming(videoProcessorResponse3.getStreamingUrl(), videoProcessorResponse3.getStreamingLength(),
                        videoProcessorResponse3.getStreamingAspectRatio(), videoProcessorResponse3.getThumbnailUrl(),
                        videoProcessorResponse3.isCertified());

        then(this.videoRepository).should(times(3)).save(any());
        then(this.videoRepository).should(times(1)).save(video);
        then(this.videoRepository).should(times(1)).save(video2);
        then(this.videoRepository).should(times(1)).save(video3);

        then(this.userService).should(times(1)).noticeNewCertified(any());
        then(this.userService).should(times(1)).noticeNewCertified(user);
    }
}
