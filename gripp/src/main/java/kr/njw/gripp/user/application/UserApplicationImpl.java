package kr.njw.gripp.user.application;

import kr.njw.gripp.user.application.dto.FindLeaderBoardAppResponse;
import kr.njw.gripp.user.application.dto.FindUserAppResponse;
import kr.njw.gripp.user.entity.User;
import kr.njw.gripp.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

@RequiredArgsConstructor
@Service
public class UserApplicationImpl implements UserApplication {
    private static final long LEADER_BOARD_TOP_BOARD_SIZE = 10;
    private static final long LEADER_BOARD_DEFAULT_BOARD_SIZE = 21;
    private static final long LEADER_BOARD_DEFAULT_BOARD_SIDE_SIZE = (LEADER_BOARD_DEFAULT_BOARD_SIZE - 1) / 2;

    private final UserRepository userRepository;
    private final EntityManager entityManager;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public FindUserAppResponse findUser(String username) {
        Optional<User> user = this.userRepository.findByUsername(username);

        if (user.isEmpty()) {
            FindUserAppResponse response = new FindUserAppResponse();
            response.setSuccess(false);
            this.logger.warn("회원이 존재하지 않습니다 - " + username);
            return response;
        }

        return this.createFindUserAppResponse(user.get());
    }

    @Transactional(readOnly = true)
    public FindLeaderBoardAppResponse findLeaderBoard(String username) {
        Optional<User> user = this.userRepository.findByUsername(username);

        if (user.isEmpty()) {
            FindLeaderBoardAppResponse response = new FindLeaderBoardAppResponse();
            response.setSuccess(false);
            this.logger.warn("회원이 존재하지 않습니다 - " + username);
            return response;
        }

        FindLeaderBoardAppResponse response = new FindLeaderBoardAppResponse();
        response.setSuccess(true);

        try (Stream<User> users = this.userRepository.findByOrderByScoreDescIdAsc()) {
            long rankEnd = this.getRankEnd();
            AtomicLong fetchedUserCount = new AtomicLong(0);
            AtomicLong lastRank = new AtomicLong(0);
            AtomicInteger lastScore = new AtomicInteger(Integer.MAX_VALUE);
            AtomicBoolean isMeFetched = new AtomicBoolean(false);
            Queue<FindUserAppResponse> defaultBoardTopQueue = new LinkedList<>();
            AtomicLong defaultBoardBottomRemainSize = new AtomicLong(LEADER_BOARD_DEFAULT_BOARD_SIDE_SIZE);

            // 유저 목록을 순회하며 각 리더보드별로 포함해야 되는 유저만 추출
            users.takeWhile(__ -> response.getTopBoard().size() < LEADER_BOARD_TOP_BOARD_SIZE
                    || defaultBoardBottomRemainSize.get() > 0).forEach(aUser -> {
                long rank;

                if (aUser.getScore() != lastScore.get()) {
                    rank = fetchedUserCount.get() + 1;
                } else {
                    rank = lastRank.get();
                }

                fetchedUserCount.getAndIncrement();
                lastRank.set(rank);
                lastScore.set(aUser.getScore());

                if (response.getTopBoard().size() < LEADER_BOARD_TOP_BOARD_SIZE) {
                    response.getTopBoard().add(this.createFindUserAppResponse(aUser, rank, rankEnd));
                }

                if (aUser == user.get()) {
                    while (!defaultBoardTopQueue.isEmpty()) {
                        response.getDefaultBoard().add(defaultBoardTopQueue.remove());
                    }

                    response.getDefaultBoard().add(this.createFindUserAppResponse(aUser, rank, rankEnd));
                    isMeFetched.set(true);
                } else {
                    if (!isMeFetched.get()) {
                        defaultBoardTopQueue.add(this.createFindUserAppResponse(aUser, rank, rankEnd));

                        if (defaultBoardTopQueue.size() > LEADER_BOARD_DEFAULT_BOARD_SIDE_SIZE) {
                            defaultBoardTopQueue.remove();
                        }
                    } else {
                        response.getDefaultBoard().add(this.createFindUserAppResponse(aUser, rank, rankEnd));
                        defaultBoardBottomRemainSize.getAndDecrement();
                    }

                    // 메모리 초과 방지
                    this.entityManager.detach(aUser);
                }
            });
        }

        return response;
    }

    private long getRank(User user) {
        return this.userRepository.countByScoreGreaterThan(user.getScore()) + 1;
    }

    private long getRankEnd() {
        return this.userRepository.countByScoreGreaterThan(0) + 1;
    }

    private int getPercentile(long rank, long rankEnd) {
        if (rank > rankEnd) {
            return 0;
        }

        return (int) (100 - ((rank * 100 + rankEnd - 1) / rankEnd));
    }

    private FindUserAppResponse createFindUserAppResponse(User user) {
        return this.createFindUserAppResponse(user, this.getRank(user), this.getRankEnd());
    }

    private FindUserAppResponse createFindUserAppResponse(User user, long rank, long rankEnd) {
        FindUserAppResponse response = new FindUserAppResponse();
        response.setSuccess(true);
        response.setUsername(user.getUsername());
        response.setTier(user.getTier());
        response.setScore(user.getScore());
        response.setRank(rank);
        response.setPercentile(this.getPercentile(rank, rankEnd));
        response.setArticleCount(user.getArticleCount());
        response.setArticleCertifiedCount(user.getArticleCertifiedCount());
        response.setRegisterDateTime(user.getRegisterDateTime());
        return response;
    }
}
