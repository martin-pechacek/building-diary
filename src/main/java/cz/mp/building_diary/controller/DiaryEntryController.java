package cz.mp.building_diary.controller;

import cz.mp.building_diary.dto.DiaryEntryDto;
import cz.mp.building_diary.dto.ErrorDto;
import cz.mp.building_diary.service.DiaryEntryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping(DiaryEntryController.URL)
@Tag(name = "Diary Entries", description = "Manage diary entries for projects")
@RequiredArgsConstructor
public class DiaryEntryController {

    public static final String URL = ProjectController.URL + "/{projectId}/diaryEntries";

    private final DiaryEntryService diaryEntryService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a diary entry for a project")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Diary entry created"),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ErrorDto.class))),
            @ApiResponse(responseCode = "401", description = "Not authenticated",
                    content = @Content(schema = @Schema(implementation = ErrorDto.class))),
            @ApiResponse(responseCode = "404", description = "Project not found",
                    content = @Content(schema = @Schema(implementation = ErrorDto.class))),
            @ApiResponse(responseCode = "409", description = "Diary entry already exists for this date",
                    content = @Content(schema = @Schema(implementation = ErrorDto.class)))
    })
    public DiaryEntryDto createDiaryEntry(@PathVariable UUID projectId, @Valid @RequestBody DiaryEntryDto request) {
        return diaryEntryService.create(projectId, request);
    }

    @GetMapping
    @Operation(summary = "Get all diary entries for project with pagination")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Diary entries page"),
            @ApiResponse(responseCode = "401", description = "Not authenticated",
                    content = @Content(schema = @Schema(implementation = ErrorDto.class))),
            @ApiResponse(responseCode = "404", description = "Project not found",
                    content = @Content(schema = @Schema(implementation = ErrorDto.class)))
    })
    public Page<DiaryEntryDto> getAllDiaryEntries(
            @PathVariable UUID projectId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return diaryEntryService.getAllByProjectId(projectId, PageRequest.of(page, size));
    }

    @GetMapping("/{date}")
    @Operation(summary = "Get diary entry by project and date")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Diary entry found"),
            @ApiResponse(responseCode = "401", description = "Not authenticated",
                    content = @Content(schema = @Schema(implementation = ErrorDto.class))),
            @ApiResponse(responseCode = "404", description = "Diary entry or project not found",
                    content = @Content(schema = @Schema(implementation = ErrorDto.class)))
    })
    public DiaryEntryDto getDiaryEntry(@PathVariable UUID projectId,
                                       @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return diaryEntryService.getByProjectIdAndDate(projectId, date);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a diary entry")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Diary entry updated"),
            @ApiResponse(responseCode = "400", description = "Validation error or project completed",
                    content = @Content(schema = @Schema(implementation = ErrorDto.class))),
            @ApiResponse(responseCode = "401", description = "Not authenticated",
                    content = @Content(schema = @Schema(implementation = ErrorDto.class))),
            @ApiResponse(responseCode = "404", description = "Diary entry not found",
                    content = @Content(schema = @Schema(implementation = ErrorDto.class)))
    })
    public DiaryEntryDto updateDiaryEntry(@PathVariable UUID projectId,
                                          @PathVariable UUID id,
                                          @Valid @RequestBody DiaryEntryDto request) {
        return diaryEntryService.update(id, request);
    }
}

