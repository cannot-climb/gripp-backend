package kr.njw.gripp.article.application.dto.search;

import kr.njw.gripp.video.entity.vo.VideoStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class SearchArticleAppRequestFilter {
    private String username;
    private String titleLike;
    private Integer minLevel;
    private Integer maxLevel;
    private Integer minAngle;
    private Integer maxAngle;
    private LocalDateTime minDateTime;
    private LocalDateTime maxDateTime;
    private List<VideoStatus> statusIn;
}
