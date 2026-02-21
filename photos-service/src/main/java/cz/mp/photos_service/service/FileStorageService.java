package cz.mp.photos_service.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

    String save(MultipartFile file);

    Resource load(String filename);

    void delete(String filename);
}
