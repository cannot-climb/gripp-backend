package kr.njw.gripp.article.repository.dto;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SearchArticleRepoPageTokenTest {

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void constructor() {
        SearchArticleRepoPageToken tokenEof = SearchArticleRepoPageToken.EOF;
        SearchArticleRepoPageToken tokenNull = new SearchArticleRepoPageToken(null);
        SearchArticleRepoPageToken tokenEmpty = new SearchArticleRepoPageToken("");
        SearchArticleRepoPageToken tokenInvalid = new SearchArticleRepoPageToken(-1, 1, 0, -1);
        SearchArticleRepoPageToken tokenInvalidFromString =
                new SearchArticleRepoPageToken(SearchArticleRepoPageToken.EOF.encode());
        SearchArticleRepoPageToken tokenValid = new SearchArticleRepoPageToken(40, 30, 20, 10);
        SearchArticleRepoPageToken tokenValidFromString =
                new SearchArticleRepoPageToken(new SearchArticleRepoPageToken(11, 21, 1, 21).encode());

        assertThat(tokenEof.getPrevId()).isEqualTo(-1);
        assertThat(tokenEof.getPrevLevel()).isEqualTo(-1);
        assertThat(tokenEof.getPrevViewCount()).isEqualTo(-1);
        assertThat(tokenEof.getPrevFavoriteCount()).isEqualTo(-1);

        assertThat(tokenNull.getPrevId()).isEqualTo(-1);
        assertThat(tokenNull.getPrevLevel()).isEqualTo(-1);
        assertThat(tokenNull.getPrevViewCount()).isEqualTo(-1);
        assertThat(tokenNull.getPrevFavoriteCount()).isEqualTo(-1);

        assertThat(tokenEmpty.getPrevId()).isEqualTo(-1);
        assertThat(tokenEmpty.getPrevLevel()).isEqualTo(-1);
        assertThat(tokenEmpty.getPrevViewCount()).isEqualTo(-1);
        assertThat(tokenEmpty.getPrevFavoriteCount()).isEqualTo(-1);

        assertThat(tokenInvalid.getPrevId()).isEqualTo(-1);
        assertThat(tokenInvalid.getPrevLevel()).isEqualTo(1);
        assertThat(tokenInvalid.getPrevViewCount()).isEqualTo(0);
        assertThat(tokenInvalid.getPrevFavoriteCount()).isEqualTo(-1);

        assertThat(tokenInvalidFromString.getPrevId()).isEqualTo(-1);
        assertThat(tokenInvalidFromString.getPrevLevel()).isEqualTo(-1);
        assertThat(tokenInvalidFromString.getPrevViewCount()).isEqualTo(-1);
        assertThat(tokenInvalidFromString.getPrevFavoriteCount()).isEqualTo(-1);

        assertThat(tokenValid.getPrevId()).isEqualTo(40);
        assertThat(tokenValid.getPrevLevel()).isEqualTo(30);
        assertThat(tokenValid.getPrevViewCount()).isEqualTo(20);
        assertThat(tokenValid.getPrevFavoriteCount()).isEqualTo(10);

        assertThat(tokenValidFromString.getPrevId()).isEqualTo(11);
        assertThat(tokenValidFromString.getPrevLevel()).isEqualTo(21);
        assertThat(tokenValidFromString.getPrevViewCount()).isEqualTo(1);
        assertThat(tokenValidFromString.getPrevFavoriteCount()).isEqualTo(21);
    }

    @Test
    void encode() {
        SearchArticleRepoPageToken tokenEof = SearchArticleRepoPageToken.EOF;
        SearchArticleRepoPageToken tokenInvalid = new SearchArticleRepoPageToken(123, 2345, 34567, -1);
        SearchArticleRepoPageToken tokenValid = new SearchArticleRepoPageToken(52, 1, 728, 0);

        String encodedTokenEof = tokenEof.encode();
        String encodedTokenInvalid = tokenInvalid.encode();
        String encodedTokenValid = tokenValid.encode();

        assertThat(encodedTokenEof).isEmpty();
        assertThat(new SearchArticleRepoPageToken(encodedTokenEof)).isEqualTo(SearchArticleRepoPageToken.EOF);

        assertThat(encodedTokenInvalid).isEmpty();
        assertThat(new SearchArticleRepoPageToken(encodedTokenInvalid)).isEqualTo(SearchArticleRepoPageToken.EOF);

        assertThat(Base64.getUrlDecoder().decode(encodedTokenValid)).hasSize(28);
        assertThat(new SearchArticleRepoPageToken(encodedTokenValid)).isEqualTo(tokenValid);
    }

    @Test
    void isValid() {
        SearchArticleRepoPageToken tokenEof = SearchArticleRepoPageToken.EOF;
        SearchArticleRepoPageToken tokenNull = new SearchArticleRepoPageToken(null);
        SearchArticleRepoPageToken tokenEmpty = new SearchArticleRepoPageToken("");
        SearchArticleRepoPageToken tokenInvalid = new SearchArticleRepoPageToken(-1, -1, -1, -1);
        SearchArticleRepoPageToken tokenInvalidFromString = new SearchArticleRepoPageToken("hihello");
        SearchArticleRepoPageToken tokenValid =
                new SearchArticleRepoPageToken(new SearchArticleRepoPageToken(1, 1, 1, 1).encode());

        boolean validTokenEof = tokenEof.isValid();
        boolean validTokenNull = tokenNull.isValid();
        boolean validTokenEmpty = tokenEmpty.isValid();
        boolean validTokenInvalid = tokenInvalid.isValid();
        boolean validTokenInvalidFromString = tokenInvalidFromString.isValid();
        boolean validTokenValid = tokenValid.isValid();

        assertThat(validTokenEof).isFalse();
        assertThat(validTokenNull).isFalse();
        assertThat(validTokenEmpty).isFalse();
        assertThat(validTokenInvalid).isFalse();
        assertThat(validTokenInvalidFromString).isFalse();
        assertThat(validTokenValid).isTrue();
    }

    @Test
    void isValidEdgeCase() {
        List<SearchArticleRepoPageToken> tokens = new ArrayList<>();

        for (int a = -2; a <= 1; a++) {
            for (int b = -2; b <= 1; b++) {
                for (int c = -2; c <= 1; c++) {
                    for (int d = -2; d <= 1; d++) {
                        tokens.add(new SearchArticleRepoPageToken(a, b, c, d));
                    }
                }
            }
        }

        List<Boolean> responses = new ArrayList<>();

        for (SearchArticleRepoPageToken token : tokens) {
            responses.add(token.isValid());
        }

        for (int i = 0; i < tokens.size(); i++) {
            SearchArticleRepoPageToken token = tokens.get(i);

            if (token.getPrevId() < 0 || token.getPrevLevel() < 0 ||
                    token.getPrevViewCount() < 0 || token.getPrevFavoriteCount() < 0) {
                assertThat(responses.get(i)).isFalse();
            } else {
                assertThat(responses.get(i)).isTrue();
            }
        }
    }
}
