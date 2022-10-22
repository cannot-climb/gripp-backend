package kr.njw.gripp.global.config;

import org.apache.tika.Tika;
import org.apache.tika.parser.mp4.MP4Parser;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TikaConfig {
    @Bean
    public Tika tika() {
        return new Tika();
    }

    @Bean
    public MP4Parser mp4Parser() {
        return new MP4Parser();
    }
}
