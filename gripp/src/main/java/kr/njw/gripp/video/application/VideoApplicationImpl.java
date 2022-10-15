package kr.njw.gripp.video.application;

import kr.njw.gripp.article.entity.Article;
import kr.njw.gripp.article.repository.ArticleRepository;
import kr.njw.gripp.global.config.GrippConfig;
import kr.njw.gripp.global.config.RabbitConfig;
import kr.njw.gripp.user.entity.User;
import kr.njw.gripp.user.repository.UserRepository;
import kr.njw.gripp.video.application.dto.FindVideoAppResponse;
import kr.njw.gripp.video.application.dto.UploadVideoAppResponse;
import kr.njw.gripp.video.entity.Video;
import kr.njw.gripp.video.entity.vo.VideoStatus;
import kr.njw.gripp.video.repository.VideoRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FilenameUtils;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.XMPDM;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.mp4.MP4Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class VideoApplicationImpl implements VideoApplication {
    private static final String EXTENSION_PATTERN = "mp4|mov";
    private static final String MIME_TYPE_PATTERN = "video/mp4|video/quicktime";
    private static final long MIN_DURATION_IN_SECONDS = 5;
    private final ArticleRepository articleRepository;
    private final UserRepository userRepository;
    private final VideoRepository videoRepository;
    private final AmqpTemplate amqpTemplate;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Transactional(rollbackFor = Exception.class)
    public UploadVideoAppResponse uploadVideo(MultipartFile file) throws IOException {
        String originalFileName = Objects.requireNonNullElse(FilenameUtils.getName(file.getOriginalFilename()), "");
        String extension = Objects.requireNonNullElse(FilenameUtils.getExtension(originalFileName), "").toLowerCase();
        this.logger.info("영상 업로드 시작 - " + originalFileName);

        if (!extension.toLowerCase().matches(EXTENSION_PATTERN)) {
            UploadVideoAppResponse response = new UploadVideoAppResponse();
            response.setSuccess(false);
            response.setMessage("invalid file extension");
            this.logger.warn("영상 확장자가 올바르지 않습니다 - " + originalFileName);
            return response;
        }

        try (InputStream inputStream = file.getInputStream()) {
            String mimeType = new Tika().detect(inputStream);

            if (!mimeType.toLowerCase().matches(MIME_TYPE_PATTERN)) {
                UploadVideoAppResponse response = new UploadVideoAppResponse();
                response.setSuccess(false);
                response.setMessage("invalid video mime type");
                this.logger.warn("영상의 MIME 타입이 올바르지 않습니다 - " + originalFileName + ", " + mimeType);
                return response;
            }
        }

        try (InputStream inputStream = file.getInputStream()) {
            MP4Parser mp4Parser = new MP4Parser();
            BodyContentHandler bodyContentHandler = new BodyContentHandler();
            Metadata metadata = new Metadata();
            ParseContext parseContext = new ParseContext();

            mp4Parser.parse(inputStream, bodyContentHandler, metadata, parseContext);
            String duration = metadata.get(XMPDM.DURATION);
            this.logger.info("영상 메타데이터 추출 완료 - " + originalFileName + ", " + metadata);

            if (duration == null) {
                UploadVideoAppResponse response = new UploadVideoAppResponse();
                response.setSuccess(false);
                response.setMessage("fail to calculate duration");
                this.logger.error("영상의 길이를 알 수 없습니다 - " + originalFileName);
                return response;
            }

            long durationInSeconds = Long.parseLong(duration.split("\\.")[0]);

            if (durationInSeconds < MIN_DURATION_IN_SECONDS) {
                UploadVideoAppResponse response = new UploadVideoAppResponse();
                response.setSuccess(false);
                response.setMessage("too short video");
                this.logger.warn("영상이 너무 짧습니다 - " + originalFileName + ", " + duration + "s");
                return response;
            }
        } catch (SAXException | TikaException e) {
            this.logger.error("Tika 에러 - " + e);
            throw new IOException(e.getMessage());
        }

        String uuid = UUID.randomUUID().toString();
        this.videoRepository.save(
                Video.builder()
                        .uuid(uuid)
                        .streamingUrl("")
                        .streamingLength(0)
                        .streamingAspectRatio(0)
                        .thumbnailUrl("")
                        .originalFileName(originalFileName)
                        .originalFileExtension(extension)
                        .status(VideoStatus.PREPROCESSING)
                        .registerDateTime(LocalDateTime.now())
                        .build()
        );

        String fileName = uuid + "." + extension;
        Path dest = Paths.get(GrippConfig.FILE_UPLOAD_PATH, fileName);
        Files.createDirectories(dest.getParent());
        file.transferTo(dest);
        this.logger.info("영상 업로드 완료 - " + originalFileName + ", " + dest);

        VideoProcessorRequest videoProcessorRequest = new VideoProcessorRequest(uuid, fileName);
        this.amqpTemplate.convertAndSend(RabbitConfig.VIDEO_PROCESSOR_QUEUE_KEY, videoProcessorRequest);
        this.logger.info("영상 처리 요청 전송 - " + videoProcessorRequest);

        UploadVideoAppResponse response = new UploadVideoAppResponse();
        response.setSuccess(true);
        response.setUuid(uuid);
        return response;
    }

    public FindVideoAppResponse findVideo(String uuid) {
        Optional<Video> video = this.videoRepository.findByUuid(uuid);

        if (video.isEmpty()) {
            FindVideoAppResponse response = new FindVideoAppResponse();
            response.setSuccess(false);
            this.logger.warn("영상이 존재하지 않습니다 - " + uuid);
            return response;
        }

        FindVideoAppResponse response = new FindVideoAppResponse();
        response.setSuccess(true);
        response.setUuid(video.get().getUuid());
        response.setStreamingUrl(video.get().getStreamingUrl());
        response.setStreamingLength(video.get().getStreamingLength());
        response.setStreamingAspectRatio(video.get().getStreamingAspectRatio());
        response.setThumbnailUrl(video.get().getThumbnailUrl());
        response.setStatus(video.get().getStatus());
        return response;
    }

    @Transactional(rollbackFor = Exception.class)
    @RabbitListener(queues = RabbitConfig.VIDEO_PROCESSOR_RETURN_QUEUE_KEY)
    public void onReturnVideoProcessor(VideoProcessorResponse response) throws IOException {
        this.logger.info("영상 처리 응답 수신 - " + response);
        Video video = this.videoRepository.findByUuidForUpdate(response.getRequest().getUuid()).orElse(null);

        if (video == null) {
            this.logger.error("영상이 존재하지 않습니다 - " + response);
            throw new RuntimeException("영상이 존재하지 않습니다 - " + response);
        }

        video.startStreaming(response.getStreamingUrl(), response.getStreamingLength(),
                response.getStreamingAspectRatio(), response.getThumbnailUrl(), response.isCertified());

        if (video.getStatus() == VideoStatus.CERTIFIED) {
            Article existedArticle = this.articleRepository.findByVideoIdForUpdate(video.getId()).orElse(null);

            if (existedArticle != null) {
                // 영상이 PREPROCESSING 상태에서 먼저 게시글로 등록되고 이후에 CERTIFIED 판정을 받은 경우
                User user = existedArticle.getUser();

                if (user != null) {
                    // 유저에게 작성했던 게시글의 CERTIFIED 판정 알림
                    user.noticeNewCertified(existedArticle);
                    this.userRepository.save(user);
                }
            }
        }

        this.videoRepository.save(video);

        Path dest = Paths.get(GrippConfig.FILE_UPLOAD_PATH, response.getRequest().getFileName());
        Files.deleteIfExists(dest);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class VideoProcessorRequest {
        private String uuid;
        private String fileName;
    }

    @Data
    private static class VideoProcessorResponse {
        private VideoProcessorRequest request;
        private String streamingUrl;
        private int streamingLength;
        private double streamingAspectRatio;
        private String thumbnailUrl;
        private boolean certified;
    }
}
