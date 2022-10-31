package kr.njw.gripp.article.entity;

import kr.njw.gripp.user.entity.User;
import kr.njw.gripp.video.entity.Video;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "article")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(toBuilder = true)
@ToString
public class Article {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude
    private User user;
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "video_id", nullable = false)
    @ToString.Exclude
    private Video video;
    @Column(length = 200, nullable = false)
    private String title;
    @Column(length = 5000, nullable = false)
    private String description;
    @Column(nullable = false)
    private int level;
    @Column(nullable = false)
    private int angle;
    @Column(nullable = false)
    private long viewCount;
    @Column(nullable = false)
    private long favoriteCount;
    @Column(nullable = false)
    private LocalDateTime registerDateTime;

    public void edit(String title, String description) {
        this.title = title;
        this.description = description;
    }
}
