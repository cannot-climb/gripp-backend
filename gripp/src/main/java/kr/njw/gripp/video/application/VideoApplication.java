package kr.njw.gripp.video.application;

import kr.njw.gripp.video.application.dto.FindVideoAppResponse;
import kr.njw.gripp.video.application.dto.UploadVideoAppResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface VideoApplication {
    UploadVideoAppResponse uploadVideo(MultipartFile file) throws IOException;

    FindVideoAppResponse findVideo(String uuid);
}
