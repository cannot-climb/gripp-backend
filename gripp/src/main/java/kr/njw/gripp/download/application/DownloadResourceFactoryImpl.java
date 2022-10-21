package kr.njw.gripp.download.application;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.nio.file.Path;

@Service
public class DownloadResourceFactoryImpl implements DownloadResourceFactory {
    public Resource createResource(Path path) {
        return new FileSystemResource(path);
    }
}
