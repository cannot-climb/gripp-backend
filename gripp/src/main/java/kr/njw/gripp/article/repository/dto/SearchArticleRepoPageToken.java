package kr.njw.gripp.article.repository.dto;

import lombok.Value;

import java.nio.ByteBuffer;
import java.util.Base64;

@Value
public class SearchArticleRepoPageToken {
    public static SearchArticleRepoPageToken EOF = new SearchArticleRepoPageToken(null);

    private static final int TOKEN_BYTE_SIZE = 8 + 4 + 8 + 8;
    private static final int NAN = -1;
    private static final int KEY_INT_A = 0x7ACDEBAB;
    private static final long KEY_LONG_A = 0x35B54B5AEC8B569DL;
    private static final long KEY_LONG_B = 0x7D76A6EDA3BAEA53L;
    private static final long KEY_LONG_C = 0x1EA22987F7ABFD2DL;

    long prevId;
    int prevLevel;
    long prevViewCount;
    long prevFavoriteCount;

    public SearchArticleRepoPageToken(long prevId, int prevLevel, long prevViewCount, long prevFavoriteCount) {
        this.prevId = prevId;
        this.prevLevel = prevLevel;
        this.prevViewCount = prevViewCount;
        this.prevFavoriteCount = prevFavoriteCount;
    }

    public SearchArticleRepoPageToken(String encodedToken) {
        byte[] bytes;

        try {
            if (encodedToken == null || encodedToken.isEmpty()) {
                throw new IllegalArgumentException();
            }

            bytes = Base64.getUrlDecoder().decode(encodedToken);

            if (bytes.length != TOKEN_BYTE_SIZE) {
                throw new IllegalArgumentException();
            }
        } catch (IllegalArgumentException ignored) {
            this.prevId = NAN;
            this.prevLevel = NAN;
            this.prevViewCount = NAN;
            this.prevFavoriteCount = NAN;
            return;
        }

        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        this.prevId = buffer.getLong() ^ KEY_LONG_A;
        this.prevLevel = buffer.getInt() ^ KEY_INT_A;
        this.prevViewCount = buffer.getLong() ^ KEY_LONG_B;
        this.prevFavoriteCount = buffer.getLong() ^ KEY_LONG_C;
    }

    public String encode() {
        if (!this.isValid()) {
            return "";
        }

        ByteBuffer buffer = ByteBuffer.allocate(TOKEN_BYTE_SIZE);
        buffer.putLong(this.prevId ^ KEY_LONG_A);
        buffer.putInt(this.prevLevel ^ KEY_INT_A);
        buffer.putLong(this.prevViewCount ^ KEY_LONG_B);
        buffer.putLong(this.prevFavoriteCount ^ KEY_LONG_C);
        buffer.flip();

        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);

        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public boolean isValid() {
        return (this.prevId > NAN) && (this.prevLevel > NAN) &&
                (this.prevViewCount > NAN) && (this.prevFavoriteCount > NAN);
    }
}
