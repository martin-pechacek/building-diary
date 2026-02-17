package cz.mp.photos_service.service.impl;

import cz.mp.photos_service.dto.PhotoDto;
import cz.mp.photos_service.entity.Photo;
import cz.mp.photos_service.exception.InvalidFileTypeException;
import cz.mp.photos_service.exception.PhotoNotFoundException;
import cz.mp.photos_service.mapper.PhotoMapper;
import cz.mp.photos_service.repository.PhotoRepository;
import cz.mp.photos_service.service.FileStorageService;
import cz.mp.photos_service.service.PhotoService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PhotoServiceImpl implements PhotoService {

    private static final Logger LOG = LoggerFactory.getLogger(PhotoServiceImpl.class);

    private final PhotoRepository photoRepository;
    private final FileStorageService fileStorageService;
    private final PhotoMapper photoMapper;

    @Override
    @Transactional
    public PhotoDto upload(UUID diaryEntryId, UUID projectId, MultipartFile file, String description) {
        if (!isValidImageFile(file)) {
            throw new InvalidFileTypeException("Invalid file type. Only JPEG and PNG images are allowed.");
        }

        String storedFilename = fileStorageService.save(file);

        Photo photo = new Photo();
        photo.setDiaryEntryId(diaryEntryId);
        photo.setProjectId(projectId);
        photo.setOwnerUserId(getCurrentUserId());
        photo.setFilename(file.getOriginalFilename());
        photo.setStoredFilename(storedFilename);
        photo.setContentType(file.getContentType());
        photo.setSize(file.getSize());
        photo.setDescription(description);

        photoRepository.save(photo);

        LOG.info("Photo uploaded for diary entry {}: {}", diaryEntryId, storedFilename);

        return photoMapper.toDto(photo);
    }

    @Override
    @Transactional(readOnly = true)
    public Resource getFile(UUID photoId) {
        Photo photo = findAndCheckAccess(photoId);
        return fileStorageService.load(photo.getStoredFilename());
    }

    @Override
    @Transactional
    public void delete(UUID photoId) {
        Photo photo = findAndCheckAccess(photoId);
        fileStorageService.delete(photo.getStoredFilename());
        photoRepository.delete(photo);

        LOG.info("Photo deleted: {}", photoId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PhotoDto> getByDiaryEntryId(UUID diaryEntryId) {
        return photoRepository.findByDiaryEntryId(diaryEntryId).stream()
                .filter(this::hasAccess)
                .map(photoMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public void deleteByDiaryEntryId(UUID diaryEntryId) {
        List<Photo> photos = photoRepository.findByDiaryEntryId(diaryEntryId);
        photos.forEach(p -> fileStorageService.delete(p.getStoredFilename()));
        photoRepository.deleteAllByDiaryEntryId(diaryEntryId);

        LOG.info("All photos deleted for diary entry: {}", diaryEntryId);
    }

    private Photo findAndCheckAccess(UUID photoId) {
        Photo photo = photoRepository.findById(photoId)
                .orElseThrow(() -> new PhotoNotFoundException("Photo not found: " + photoId));
        if (!hasAccess(photo)) {
            throw new PhotoNotFoundException("Photo not found: " + photoId);
        }
        return photo;
    }

    private boolean hasAccess(Photo photo) {
        if (isAdmin()) {
            return true;
        }
        return photo.getOwnerUserId().equals(getCurrentUserId());
    }

    private String getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth.getName();
    }

    private boolean isAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
    }

    private boolean isValidImageFile(MultipartFile file) {
        try (InputStream is = file.getInputStream()) {
            byte[] header = new byte[8];
            if (is.read(header) < 8) {
                return false;
            }
            // JPEG: FF D8 FF
            if (header[0] == (byte) 0xFF && header[1] == (byte) 0xD8 && header[2] == (byte) 0xFF) {
                return true;
            }
            // PNG: 89 50 4E 47 0D 0A 1A 0A
            byte[] pngSignature = {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};
            return Arrays.equals(header, pngSignature);
        } catch (IOException e) {
            return false;
        }
    }
}
