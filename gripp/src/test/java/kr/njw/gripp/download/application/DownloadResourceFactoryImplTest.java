package kr.njw.gripp.download.application;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

class DownloadResourceFactoryImplTest {
    private DownloadResourceFactoryImpl downloadResourceFactoryImpl;

    @BeforeEach
    void setUp() {
        this.downloadResourceFactoryImpl = new DownloadResourceFactoryImpl();
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void createResource() {
        Resource no = this.downloadResourceFactoryImpl.createResource(Paths.get("no"));

        assertThat(no).isInstanceOf(FileSystemResource.class);
        assertThat(no.getFilename()).isEqualTo("no");
    }
}
