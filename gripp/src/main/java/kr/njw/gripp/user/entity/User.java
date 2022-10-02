package kr.njw.gripp.user.entity;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

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
    private int score;
    @Column(nullable = false)
    private int articleCount;
    @Column(nullable = false)
    private int articleCertifiedCount;
    @Column(nullable = false)
    private LocalDateTime registerDateTime;

    public int getTier() {
        return (this.score + 50) / 100;
    }
}
