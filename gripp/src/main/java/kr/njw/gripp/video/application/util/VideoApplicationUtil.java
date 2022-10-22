package kr.njw.gripp.video.application.util;

import kr.njw.gripp.video.application.dto.FindVideoAppResponse;
import kr.njw.gripp.video.entity.Video;

public interface VideoApplicationUtil {
    static FindVideoAppResponse createFindVideoAppResponse(Video video) {
        FindVideoAppResponse response = new FindVideoAppResponse();
        response.setSuccess(true);
        response.setUuid(video.getUuid());
        response.setStreamingUrl(video.getStreamingUrl());
        response.setStreamingLength(video.getStreamingLength());
        response.setStreamingAspectRatio(video.getStreamingAspectRatio());
        response.setThumbnailUrl(video.getThumbnailUrl());
        response.setStatus(video.getStatus());
        return response;
    }
}
