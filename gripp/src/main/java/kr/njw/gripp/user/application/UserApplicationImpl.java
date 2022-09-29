package kr.njw.gripp.user.application;

import kr.njw.gripp.user.application.dto.FindUserAppResponse;
import kr.njw.gripp.user.entity.User;
import kr.njw.gripp.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class UserApplicationImpl implements UserApplication {
    private final UserRepository userRepository;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public FindUserAppResponse findUser(String username) {
        Optional<User> user = this.userRepository.findByUsername(username);

        if (user.isEmpty()) {
            FindUserAppResponse response = new FindUserAppResponse();
            response.setSuccess(false);
            this.logger.warn("회원이 존재하지 않습니다 - " + username);
            return response;
        }

        long rank = this.getRank(user.get());
        int percentile = this.getPercentile(rank);

        FindUserAppResponse response = new FindUserAppResponse();
        response.setSuccess(true);
        response.setUsername(user.get().getUsername());
        response.setTier(user.get().getTier());
        response.setScore(user.get().getScore());
        response.setRank(rank);
        response.setPercentile(percentile);
        response.setArticleCount(user.get().getArticleCount());
        response.setArticleCertifiedCount(user.get().getArticleCertifiedCount());
        response.setRegisterDateTime(user.get().getRegisterDateTime());
        return response;
    }

    private long getRank(User user) {
        return this.userRepository.countByScoreGreaterThan(Objects.requireNonNullElse(user.getScore(), 0))
                + 1;
    }

    private int getPercentile(long rank) {
        long total = this.userRepository.countByScoreGreaterThan(0);

        if (rank > total) {
            return 0;
        }

        return (int) (100 - ((rank * 100 + total - 1) / total));
    }
}
