package kr.njw.gripp.article.controller.dto.search;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import kr.njw.gripp.video.entity.vo.VideoStatus;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class SearchArticleRequestFilter implements
        SearchArticleRequestTitleFilter, SearchArticleRequestUserFilter,
        SearchArticleRequestLevelFilter, SearchArticleRequestAngleFilter,
        SearchArticleRequestDateTimeFilter, SearchArticleRequestStatusFilter {
    @Hidden
    @NotNull(message = "must not be null")
    private SearchArticleRequestFilterType type;
    @Hidden
    private String username;
    @Hidden
    private String titleLike;
    @Hidden
    private Integer minLevel;
    @Hidden
    private Integer maxLevel;
    @Hidden
    private Integer minAngle;
    @Hidden
    private Integer maxAngle;
    @Hidden
    private LocalDateTime minDateTime;
    @Hidden
    private LocalDateTime maxDateTime;
    @Hidden
    private List<VideoStatus> statusIn;
}

interface SearchArticleRequestTitleFilter {
    @Schema(type = "string", allowableValues = "TITLE", required = true)
    SearchArticleRequestFilterType getType();

    @Schema(example = "the wall", required = true)
    String getTitleLike();
}

interface SearchArticleRequestUserFilter {
    @Schema(type = "string", allowableValues = "USER", required = true)
    SearchArticleRequestFilterType getType();

    @Schema(example = "njw1204", required = true)
    String getUsername();
}

interface SearchArticleRequestLevelFilter {
    @Schema(type = "string", allowableValues = "LEVEL", required = true)
    SearchArticleRequestFilterType getType();

    @Schema(example = "0", required = true)
    Integer getMinLevel();

    @Schema(example = "19", required = true)
    Integer getMaxLevel();
}

interface SearchArticleRequestAngleFilter {
    @Schema(type = "string", allowableValues = "ANGLE", required = true)
    SearchArticleRequestFilterType getType();

    @Schema(example = "0", required = true)
    Integer getMinAngle();

    @Schema(example = "70", required = true)
    Integer getMaxAngle();
}

interface SearchArticleRequestDateTimeFilter {
    @Schema(type = "string", allowableValues = "DATETIME", required = true)
    SearchArticleRequestFilterType getType();

    @Schema(type = "string", example = "2022-09-17 11:37:09", required = true)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime getMinDateTime();

    @Schema(type = "string", example = "2023-12-04 01:05:59", required = true)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime getMaxDateTime();
}

interface SearchArticleRequestStatusFilter {
    @Schema(type = "string", allowableValues = "STATUS", required = true)
    SearchArticleRequestFilterType getType();

    @ArraySchema(arraySchema = @Schema(example = "[\"NO_CERTIFIED\", \"CERTIFIED\"]", required = true))
    List<VideoStatus> getStatusIn();
}
