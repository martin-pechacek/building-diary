package cz.mp.building_diary.config;

import cz.mp.building_diary.exception.FileStorageException;
import cz.mp.building_diary.properties.StorageProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
@EnableConfigurationProperties(StorageProperties.class)
public class StorageConfig {

    @Bean
    public Path storageLocation(StorageProperties storageProperties) {
        Path location = Paths.get(storageProperties.location()).toAbsolutePath().normalize();
        try {
            Files.createDirectories(location);
        } catch (IOException e) {
            throw new FileStorageException("Could not create storage directory", e);
        }
        return location;
    }
}