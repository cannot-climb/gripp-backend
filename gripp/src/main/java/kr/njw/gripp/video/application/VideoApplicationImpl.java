package kr.njw.gripp.video.application;

import kr.njw.gripp.article.entity.Article;
import kr.njw.gripp.article.repository.ArticleRepository;
import kr.njw.gripp.global.config.GrippConfig;
import kr.njw.gripp.global.config.RabbitConfig;
import kr.njw.gripp.user.entity.User;
import kr.njw.gripp.user.repository.UserRepository;
import kr.njw.gripp.user.service.UserService;
import kr.njw.gripp.video.application.dto.FindVideoAppResponse;
import kr.njw.gripp.video.application.dto.UploadVideoAppResponse;
import kr.njw.gripp.video.application.util.VideoApplicationUtil;
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
    private static final long MIN_DURATION_IN_SECONDS = 1;

    private final UserService userService;
    private final ArticleRepository articleRepository;
    private final UserRepository userRepository;
    private final VideoRepository videoRepository;
    private final AmqpTemplate amqpTemplate;
    private final Tika tika;
    private final MP4Parser mp4Parser;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Transactional(rollbackFor = Exception.class)
    public UploadVideoAppResponse uploadVideo(MultipartFile file) throws IOException {
        String originalFileName = Objects.requireNonNullElse(FilenameUtils.getName(file.getOriginalFilename()), "");
        String extension = Objects.requireNonNullElse(FilenameUtils.getExtension(originalFileName), "").toLowerCase();
        this.logger.info("?????? ????????? ?????? - " + originalFileName);

        if (!extension.toLowerCase().matches(EXTENSION_PATTERN)) {
            UploadVideoAppResponse response = new UploadVideoAppResponse();
            response.setSuccess(false);
            response.setMessage("invalid file extension");
            this.logger.warn("?????? ???????????? ???????????? ???????????? - " + originalFileName);
            return response;
        }

        try (InputStream inputStream = file.getInputStream()) {
            String mimeType = this.tika.detect(inputStream);

            if (!mimeType.toLowerCase().matches(MIME_TYPE_PATTERN)) {
                UploadVideoAppResponse response = new UploadVideoAppResponse();
                response.setSuccess(false);
                response.setMessage("invalid video mime type");
                this.logger.warn("????????? MIME ????????? ???????????? ???????????? - " + originalFileName + ", " + mimeType);
                return response;
            }
        }

        try (InputStream inputStream = file.getInputStream()) {
            BodyContentHandler bodyContentHandler = new BodyContentHandler();
            Metadata metadata = new Metadata();
            ParseContext parseContext = new ParseContext();

            this.mp4Parser.parse(inputStream, bodyContentHandler, metadata, parseContext);
            String duration = metadata.get(XMPDM.DURATION);
            this.logger.info("?????? ??????????????? ?????? ?????? - " + originalFileName + ", " + metadata);

            if (duration == null) {
                UploadVideoAppResponse response = new UploadVideoAppResponse();
                response.setSuccess(false);
                response.setMessage("fail to calculate duration");
                this.logger.error("????????? ????????? ??? ??? ???????????? - " + originalFileName);
                return response;
            }

            long durationInSeconds = Long.parseLong(duration.split("\\.")[0]);

            if (durationInSeconds < MIN_DURATION_IN_SECONDS) {
                UploadVideoAppResponse response = new UploadVideoAppResponse();
                response.setSuccess(false);
                response.setMessage("too short video");
                this.logger.warn("????????? ?????? ???????????? - " + originalFileName + ", " + duration + "s");
                return response;
            }
        } catch (SAXException | TikaException e) {
            this.logger.error("Tika ?????? - " + e);
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
        this.logger.info("?????? ????????? ?????? - " + originalFileName + ", " + dest);

        VideoProcessorRequest videoProcessorRequest = new VideoProcessorRequest(uuid, fileName);
        this.amqpTemplate.convertAndSend(RabbitConfig.VIDEO_PROCESSOR_QUEUE_KEY, videoProcessorRequest);
        this.logger.info("?????? ?????? ?????? ?????? - " + videoProcessorRequest);

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
            this.logger.warn("????????? ???????????? ???????????? - " + uuid);
            return response;
        }

        return VideoApplicationUtil.createFindVideoAppResponse(video.get());
    }

    @Transactional(rollbackFor = Exception.class)
    @RabbitListener(queues = RabbitConfig.VIDEO_PROCESSOR_RETURN_QUEUE_KEY)
    public void onReturnVideoProcessor(VideoProcessorResponse response) throws IOException {
        this.logger.info("?????? ?????? ?????? ?????? - " + response);
        Video video = this.videoRepository.findForUpdateByUuid(response.getRequest().getUuid()).orElse(null);

        if (video == null) {
            this.logger.error("????????? ???????????? ???????????? - " + response);
            throw new RuntimeException("????????? ???????????? ???????????? - " + response);
        }

        video.startStreaming(response.getStreamingUrl(), response.getStreamingLength(),
                response.getStreamingAspectRatio(), response.getThumbnailUrl(), response.isCertified());

        if (video.isCertified()) {
            Article existedArticle = this.articleRepository.findForShareByVideoId(video.getId()).orElse(null);

            if (existedArticle != null && existedArticle.getUser() != null) {
                // ????????? PREPROCESSING ???????????? ?????? ???????????? ???????????? ????????? CERTIFIED ????????? ?????? ??????
                User user = this.userRepository.findForUpdateById(existedArticle.getUser().getId()).orElseThrow();
                // ???????????? ???????????? ???????????? CERTIFIED ?????? ??????
                this.userService.noticeNewCertified(user);
            }
        }

        this.videoRepository.save(video);

        Path dest = Paths.get(GrippConfig.FILE_UPLOAD_PATH, response.getRequest().getFileName());
        Files.deleteIfExists(dest);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VideoProcessorRequest {
        private String uuid;
        private String fileName;
    }

    @Data
    public static class VideoProcessorResponse {
        private VideoProcessorRequest request;
        private String streamingUrl;
        private int streamingLength;
        private double streamingAspectRatio;
        private String thumbnailUrl;
        private boolean certified;
    }
}
