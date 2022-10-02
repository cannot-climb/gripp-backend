package kr.njw.gripp.video.application;

import kr.njw.gripp.video.application.dto.UploadVideoAppResponse;
import kr.njw.gripp.video.entity.Video;
import kr.njw.gripp.video.entity.vo.VideoStatus;
import kr.njw.gripp.video.repository.VideoRepository;
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
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class VideoApplicationImpl implements VideoApplication {
    private static final String UPLOAD_DIRECTORY = "upload/";
    private static final String EXTENSION_PATTERN = "mp4|mov";
    private static final String MIME_TYPE_PATTERN = "video/mp4|video/quicktime";
    private static final long MIN_DURATION_IN_SECONDS = 5;

    private final VideoRepository videoRepository;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Transactional(rollbackFor = Exception.class)
    public UploadVideoAppResponse uploadVideo(MultipartFile file) throws IOException {
        String originalFileName = Objects.requireNonNullElse(FilenameUtils.getName(file.getOriginalFilename()), "");
        String extension = Objects.requireNonNullElse(FilenameUtils.getExtension(originalFileName), "");

        if (!extension.matches(EXTENSION_PATTERN)) {
            UploadVideoAppResponse response = new UploadVideoAppResponse();
            response.setSuccess(false);
            response.setMessage("invalid file extension");
            this.logger.warn("영상 확장자가 올바르지 않습니다 - " + originalFileName);
            return response;
        }

        try (InputStream inputStream = file.getInputStream()) {
            String mimeType = new Tika().detect(inputStream);

            if (!mimeType.matches(MIME_TYPE_PATTERN)) {
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
            throw new IOException(e.getMessage());
        }

        String uuid = UUID.randomUUID().toString();
        this.videoRepository.save(Video.builder()
                .uuid(uuid)
                .originalFileName(originalFileName)
                .originalFileExtension(extension)
                .status(VideoStatus.PREPROCESSING)
                .registerDateTime(LocalDateTime.now())
                .build());

        String fileName = uuid + "." + extension;
        Path dest = Paths.get(UPLOAD_DIRECTORY, fileName);
        Files.createDirectories(dest.getParent());
        file.transferTo(dest);
        this.logger.info("영상 업로드 완료 - " + originalFileName + ", " + dest);

        UploadVideoAppResponse response = new UploadVideoAppResponse();
        response.setSuccess(true);
        response.setUuid(uuid);
        return response;
    }
}
