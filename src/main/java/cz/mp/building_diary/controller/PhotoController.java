package cz.mp.building_diary.controller;

import cz.mp.building_diary.dto.ErrorDto;
import cz.mp.building_diary.dto.PhotoDto;
import cz.mp.building_diary.service.PhotoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping(PhotoController.URL)
@Tag(name = "Photos", description = "Photo management endpoints")
@RequiredArgsConstructor
public class PhotoController extends BaseController {

    public static final String URL = BASE_PATH + "/photos";

    private final PhotoService photoService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Upload a photo to a diary entry")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Photo uploaded"),
            @ApiResponse(responseCode = "401", description = "Not authenticated",
                    content = @Content(schema = @Schema(implementation = ErrorDto.class))),
            @ApiResponse(responseCode = "404", description = "Diary entry not found",
                    content = @Content(schema = @Schema(implementation = ErrorDto.class))),
            @ApiResponse(responseCode = "500", description = "Upload failed",
                    content = @Content(schema = @Schema(implementation = ErrorDto.class)))
    })
    public PhotoDto upload(@RequestParam UUID diaryEntryId,
                           @RequestParam MultipartFile file,
                           @RequestParam(required = false) String description) {
        return photoService.upload(diaryEntryId, file, description);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Download a photo")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Photo file"),
            @ApiResponse(responseCode = "401", description = "Not authenticated",
                    content = @Content(schema = @Schema(implementation = ErrorDto.class))),
            @ApiResponse(responseCode = "404", description = "Photo not found",
                    content = @Content(schema = @Schema(implementation = ErrorDto.class)))
    })
    public ResponseEntity<Resource> download(@PathVariable UUID id) {
        Resource resource = photoService.getFile(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a photo")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Photo deleted"),
            @ApiResponse(responseCode = "401", description = "Not authenticated",
                    content = @Content(schema = @Schema(implementation = ErrorDto.class))),
            @ApiResponse(responseCode = "404", description = "Photo not found",
                    content = @Content(schema = @Schema(implementation = ErrorDto.class)))
    })
    public void delete(@PathVariable UUID id) {
        photoService.delete(id);
    }
}