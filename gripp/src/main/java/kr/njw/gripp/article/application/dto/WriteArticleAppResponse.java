package kr.njw.gripp.article.application.dto;

import lombok.Data;

import java.util.Optional;

@Data
public class WriteArticleAppResponse {
    private WriteArticleAppResponseStatus status = WriteArticleAppResponseStatus.SUCCESS;
    private Long id;

    public Optional<Long> getId() {
        return Optional.ofNullable(this.id);
    }
}
