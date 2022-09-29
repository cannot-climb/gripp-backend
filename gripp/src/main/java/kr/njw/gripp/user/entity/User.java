package kr.njw.gripp.user.entity;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "user")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@ToString
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(length = 100, nullable = false, unique = true)
    private String username;
    @Column(nullable = false)
    private Integer score;
    @Column(nullable = false)
    private Integer articleCount;
    @Column(nullable = false)
    private Integer articleCertifiedCount;
    @Column(nullable = false)
    private LocalDateTime registerDateTime;

    public int getTier() {
        return (Objects.requireNonNullElse(this.score, 0) + 50) / 100;
    }
}
