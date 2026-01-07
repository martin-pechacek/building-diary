package cz.mp.building_diary.service.impl;

import cz.mp.building_diary.dto.PhotoDto;
import cz.mp.building_diary.entity.DiaryEntry;
import cz.mp.building_diary.entity.Photo;
import cz.mp.building_diary.exception.DiaryEntryNotFoundException;
import cz.mp.building_diary.exception.InvalidFileTypeException;
import cz.mp.building_diary.exception.PhotoNotFoundException;
import cz.mp.building_diary.mapper.PhotoMapper;
import cz.mp.building_diary.repository.DiaryEntryRepository;
import cz.mp.building_diary.repository.PhotoRepository;
import cz.mp.building_diary.service.FileStorageService;
import cz.mp.building_diary.service.PhotoService;
import cz.mp.building_diary.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PhotoServiceImpl implements PhotoService {

    private static final Logger LOG = LoggerFactory.getLogger(PhotoServiceImpl.class);

    private final PhotoRepository photoRepository;
    private final DiaryEntryRepository diaryEntryRepository;
    private final FileStorageService fileStorageService;
    private final ProjectService projectService;
    private final PhotoMapper photoMapper;

    @Override
    @Transactional
    public PhotoDto upload(UUID diaryEntryId, MultipartFile file, String description) {
        if (!isValidImageFile(file)) {
            throw new InvalidFileTypeException("Invalid file type. Only JPEG and PNG images are allowed.");
        }

        DiaryEntry diaryEntry = diaryEntryRepository.findById(diaryEntryId)
                .orElseThrow(() -> new DiaryEntryNotFoundException("Diary entry not found: " + diaryEntryId));

        projectService.hasAccess(diaryEntry.getProject().getId());

        String storedFilename = fileStorageService.save(file);

        Photo photo = new Photo();
        photo.setFilename(file.getOriginalFilename());
        photo.setStoredFilename(storedFilename);
        photo.setContentType(file.getContentType());
        photo.setSize(file.getSize());
        photo.setDescription(description);

        diaryEntry.addPhoto(photo);
        photoRepository.save(photo);

        LOG.info("Photo uploaded for diary entry {}: {}", diaryEntryId, storedFilename);

        return photoMapper.toDto(photo);
    }

    @Override
    @Transactional(readOnly = true)
    public Resource getFile(UUID photoId) {
        Photo photo = photoRepository.findById(photoId)
                .orElseThrow(() -> new PhotoNotFoundException("Photo not found: " + photoId));

        projectService.hasAccess(photo.getDiaryEntry().getProject().getId());

        return fileStorageService.load(photo.getStoredFilename());
    }

    @Override
    @Transactional
    public void delete(UUID photoId) {
        Photo photo = photoRepository.findById(photoId)
                .orElseThrow(() -> new PhotoNotFoundException("Photo not found: " + photoId));

        projectService.hasAccess(photo.getDiaryEntry().getProject().getId());

        fileStorageService.delete(photo.getStoredFilename());
        photoRepository.delete(photo);

        LOG.info("Photo deleted: {}", photoId);
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