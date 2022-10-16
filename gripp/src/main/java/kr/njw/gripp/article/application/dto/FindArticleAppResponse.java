package kr.njw.gripp.article.application.dto;

import kr.njw.gripp.user.application.dto.FindUserAppResponse;
import kr.njw.gripp.video.application.dto.FindVideoAppResponse;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Optional;

@Data
public class FindArticleAppResponse {
    private FindArticleAppResponseStatus status = FindArticleAppResponseStatus.SUCCESS;
    private Long id;
    private FindUserAppResponse user = new FindUserAppResponse();
    private FindVideoAppResponse video = new FindVideoAppResponse();
    private String title = "";
    private String description = "";
    private int level;
    private int angle;
    private long viewCount;
    private long favoriteCount;
    private LocalDateTime registerDateTime;
    private boolean favorite;

    public Optional<Long> getId() {
        return Optional.ofNullable(this.id);
    }

    public Optional<LocalDateTime> getRegisterDateTime() {
        return Optional.ofNullable(this.registerDateTime);
    }
}
