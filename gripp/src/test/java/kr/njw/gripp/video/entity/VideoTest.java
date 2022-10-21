package kr.njw.gripp.video.entity;

import kr.njw.gripp.video.entity.vo.VideoStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

class VideoTest {
    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void startStreaming() {
        Video video = Video.builder().build();
        Video video2 = Video.builder().status(VideoStatus.PREPROCESSING).build();

        video.startStreaming("stream", 1, 2, "thumb", true);
        video2.startStreaming("test", 2, 1, "love", false);
        Throwable throwable = catchThrowable(() -> video2.startStreaming("stream", 1, 2, "thumb", true));

        assertThat(video.getStreamingUrl()).isEqualTo("stream");
        assertThat(video.getStreamingLength()).isEqualTo(1);
        assertThat(video.getStreamingAspectRatio()).isEqualTo(2);
        assertThat(video.getThumbnailUrl()).isEqualTo("thumb");
        assertThat(video.getStatus()).isEqualTo(VideoStatus.CERTIFIED);

        assertThat(video2.getStreamingUrl()).isEqualTo("test");
        assertThat(video2.getStreamingLength()).isEqualTo(2);
        assertThat(video2.getStreamingAspectRatio()).isEqualTo(1);
        assertThat(video2.getThumbnailUrl()).isEqualTo("love");
        assertThat(video2.getStatus()).isEqualTo(VideoStatus.NO_CERTIFIED);

        assertThat(throwable).isInstanceOf(RuntimeException.class);
    }

    @Test
    void isCertified() {
        Video video = Video.builder().status(VideoStatus.PREPROCESSING).build();
        Video video2 = Video.builder().status(VideoStatus.NO_CERTIFIED).build();
        Video video3 = Video.builder().status(VideoStatus.CERTIFIED).build();

        assertThat(video.isCertified()).isFalse();
        assertThat(video2.isCertified()).isFalse();
        assertThat(video3.isCertified()).isTrue();
    }
}
