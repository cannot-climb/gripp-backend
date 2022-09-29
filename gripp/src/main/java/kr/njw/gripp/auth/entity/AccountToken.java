package kr.njw.gripp.auth.entity;

import lombok.*;
import org.apache.commons.lang3.RandomStringUtils;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_token")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@ToString
public class AccountToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private Account account;
    @Column(length = 100, nullable = false, unique = true)
    private String refreshToken;
    @Column(nullable = false)
    private LocalDateTime registerDateTime;
    @Column(nullable = false)
    private LocalDateTime expireDateTime;

    public void rotate() {
        if (this.account == null) {
            throw new RuntimeException("no account");
        }

        this.refreshToken = RandomStringUtils.randomAlphanumeric(64);
        this.registerDateTime = LocalDateTime.now();
    }

    public void issue() {
        if (this.account == null) {
            throw new RuntimeException("no account");
        }

        this.rotate();
        this.expireDateTime = this.registerDateTime.plusDays(30);
    }

    public boolean isExpired() {
        return !LocalDateTime.now().isBefore(this.expireDateTime);
    }
}
