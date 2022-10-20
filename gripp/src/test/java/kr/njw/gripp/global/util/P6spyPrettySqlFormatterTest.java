package kr.njw.gripp.global.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class P6spyPrettySqlFormatterTest {
    private P6spyPrettySqlFormatter p6spyPrettySqlFormatter;

    @BeforeEach
    void setUp() {
        this.p6spyPrettySqlFormatter = new P6spyPrettySqlFormatter();
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void formatMessage() {
        String message = this.p6spyPrettySqlFormatter.formatMessage(0, null, 100, "commit", null, " ", null);
        String message2 = this.p6spyPrettySqlFormatter.formatMessage(0, null, 1111, "commit", null, null, null);
        String message3 = this.p6spyPrettySqlFormatter.formatMessage(0, null, 52, "commit", null, "hello", null);
        String message4 = this.p6spyPrettySqlFormatter.formatMessage(
                0, null, 24, "statement", null, "SELECT id,id2 FROM test WHERE 1=1 AND 2=2 ORDER BY id DESC", null);
        String message5 = this.p6spyPrettySqlFormatter.formatMessage(
                0, null, 1, "statement", null, "CREATE TABLE test(id int,id2 varchar(100))", null);
        String message6 = this.p6spyPrettySqlFormatter.formatMessage(
                0, null, 1, "statement", null, "ALTER TABLE test ADD COLUMN count SMALLINT(6)", null);
        String message7 = this.p6spyPrettySqlFormatter.formatMessage(
                0, null, 1, "statement", null, "comment on table asdf is 'abc'", null);

        assertThat(message).isEqualTo("commit completed in 100 ms");
        assertThat(message2).isEqualTo("commit completed in 1111 ms");
        assertThat(message3).isEqualTo("commit completed in 52 mshello");
        assertThat(message4).isEqualTo("""
                statement completed in 24 ms
                    SELECT
                        id,
                        id2\040
                    FROM
                        test\040
                    WHERE
                        1=1\040
                        AND 2=2\040
                    ORDER BY
                        id DESC""");
        assertThat(message5).isEqualTo("""
                statement completed in 1 ms
                    CREATE TABLE test(
                       id int,
                       id2 varchar(100)
                    )""");
        assertThat(message6).isEqualTo("""
                statement completed in 1 ms
                    ALTER TABLE test ADD COLUMN count SMALLINT(6)""");
        assertThat(message7).isEqualTo("""
                statement completed in 1 ms
                    comment on table asdf is
                        'abc'""");
    }
}
