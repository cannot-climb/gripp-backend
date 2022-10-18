package kr.njw.gripp.article.repository.dto;

import kr.njw.gripp.video.entity.vo.VideoStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class SearchArticleRepoRequestFilter {
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
