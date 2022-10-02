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
    @Column(columnDefinition = "TEXT", nullable = false)
    private String originalFileName;
    @Column(length = 10, nullable = false)
    private String originalFileExtension;
    @Column(nullable = false)
    private VideoStatus status;
    @Column(nullable = false)
    private LocalDateTime registerDateTime;
}
