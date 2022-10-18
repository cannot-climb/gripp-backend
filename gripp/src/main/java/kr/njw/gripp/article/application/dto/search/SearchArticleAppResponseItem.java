package kr.njw.gripp.article.application.dto.search;

import kr.njw.gripp.video.application.dto.FindVideoAppResponse;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Optional;

@Data
public class SearchArticleAppResponseItem {
    private Long id;
    private String username = "";
    private FindVideoAppResponse video = new FindVideoAppResponse();
    private String title = "";
    private String description = "";
    private int level;
    private int angle;
    private long viewCount;
    private long favoriteCount;
    private LocalDateTime registerDateTime;

    public Optional<Long> getId() {
        return Optional.ofNullable(this.id);
    }

    public Optional<LocalDateTime> getRegisterDateTime() {
        return Optional.ofNullable(this.registerDateTime);
    }
}
