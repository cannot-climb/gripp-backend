package kr.njw.gripp.article.application.dto;

import lombok.Data;

@Data
public class WriteArticleAppRequest {
    private String username;
    private String videoUuid;
    private String title;
    private String description;
    private int level;
    private int angle;
}
