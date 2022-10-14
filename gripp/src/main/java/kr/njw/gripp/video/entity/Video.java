package kr.njw.gripp.video.entity;

import kr.njw.gripp.video.entity.vo.VideoStatus;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "video")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@ToString
public class Video {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(length = 36, nullable = false, unique = true)
    private String uuid;
    @Column(length = 500, nullable = false)
    private String streamingUrl;
    @Column(nullable = false)
    private int streamingLength;
    @Column(nullable = false)
    private double streamingAspectRatio;
    @Column(length = 500, nullable = false)
    private String thumbnailUrl;
    @Column(length = 500, nullable = false)
    private String originalFileName;
    @Column(length = 10, nullable = false)
    private String originalFileExtension;
    @Column(nullable = false)
    private VideoStatus status;
    @Column(nullable = false)
    private LocalDateTime registerDateTime;

    public void startStreaming(String streamingUrl, int streamingLength, double streamingAspectRatio,
                               String thumbnailUrl, boolean certified) {
        this.streamingUrl = streamingUrl;
        this.streamingLength = streamingLength;
        this.streamingAspectRatio = streamingAspectRatio;
        this.thumbnailUrl = thumbnailUrl;
        this.status = certified ? VideoStatus.CERTIFIED : VideoStatus.NO_CERTIFIED;
    }
}
