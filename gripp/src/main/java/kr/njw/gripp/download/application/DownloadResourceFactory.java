package kr.njw.gripp.download.application;

import org.springframework.core.io.Resource;

import java.nio.file.Path;

public interface DownloadResourceFactory {
    Resource createResource(Path path);
}
