package cz.mp.building_diary.controller;

import cz.mp.building_diary.dto.DiaryEntryDto;
import cz.mp.building_diary.dto.ErrorDto;
import cz.mp.building_diary.dto.ProjectDto;
import cz.mp.building_diary.service.DiaryEntryService;
import cz.mp.building_diary.service.ProjectService;
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
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
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
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(ProjectController.URL)
@Tag(name = "Projects", description = "Project management endpoints")
@RequiredArgsConstructor
public class ProjectController extends BaseController {

    public static final String URL = BASE_PATH + "/projects";

    private final ProjectService projectService;
    private final DiaryEntryService diaryEntryService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new project")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Project created"),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ErrorDto.class))),
            @ApiResponse(responseCode = "401", description = "Not authenticated",
                    content = @Content(schema = @Schema(implementation = ErrorDto.class)))
    })
    public ProjectDto create(@Valid @RequestBody ProjectDto request) {
        return projectService.create(request);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get project by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Project found"),
            @ApiResponse(responseCode = "401", description = "Not authenticated",
                    content = @Content(schema = @Schema(implementation = ErrorDto.class))),
            @ApiResponse(responseCode = "404", description = "Project not found",
                    content = @Content(schema = @Schema(implementation = ErrorDto.class)))
    })
    public ProjectDto getById(@PathVariable UUID id) {
        return projectService.getById(id);
    }

    @GetMapping
    @Operation(summary = "Get all projects for current user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Projects list"),
            @ApiResponse(responseCode = "401", description = "Not authenticated",
                    content = @Content(schema = @Schema(implementation = ErrorDto.class)))
    })
    public List<ProjectDto> getAll() {
        return projectService.getAll();
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update project")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Project updated"),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ErrorDto.class))),
            @ApiResponse(responseCode = "401", description = "Not authenticated",
                    content = @Content(schema = @Schema(implementation = ErrorDto.class))),
            @ApiResponse(responseCode = "404", description = "Project not found",
                    content = @Content(schema = @Schema(implementation = ErrorDto.class)))
    })
    public ProjectDto update(@PathVariable UUID id, @Valid @RequestBody ProjectDto request) {
        return projectService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Archive project")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Project archived"),
            @ApiResponse(responseCode = "401", description = "Not authenticated",
                    content = @Content(schema = @Schema(implementation = ErrorDto.class))),
            @ApiResponse(responseCode = "404", description = "Project not found",
                    content = @Content(schema = @Schema(implementation = ErrorDto.class)))
    })
    public void archive(@PathVariable UUID id) {
        projectService.archive(id);
    }

    @PostMapping("/{id}/start")
    @Operation(summary = "Start project construction")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Project started"),
            @ApiResponse(responseCode = "400", description = "Cannot start project - requirements not met",
                    content = @Content(schema = @Schema(implementation = ErrorDto.class))),
            @ApiResponse(responseCode = "401", description = "Not authenticated",
                    content = @Content(schema = @Schema(implementation = ErrorDto.class))),
            @ApiResponse(responseCode = "404", description = "Project not found",
                    content = @Content(schema = @Schema(implementation = ErrorDto.class)))
    })
    public ProjectDto start(@PathVariable UUID id) {
        return projectService.start(id);
    }

    @PostMapping("/{id}/complete")
    @Operation(summary = "Complete project construction")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Project completed"),
            @ApiResponse(responseCode = "400", description = "Cannot complete project - requirements not met",
                    content = @Content(schema = @Schema(implementation = ErrorDto.class))),
            @ApiResponse(responseCode = "401", description = "Not authenticated",
                    content = @Content(schema = @Schema(implementation = ErrorDto.class))),
            @ApiResponse(responseCode = "404", description = "Project not found",
                    content = @Content(schema = @Schema(implementation = ErrorDto.class)))
    })
    public ProjectDto complete(@PathVariable UUID id) {
        return projectService.complete(id);
    }

    @PostMapping("/{id}/diaryEntry")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create diary entry for project")
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
    public DiaryEntryDto createDiaryEntry(@PathVariable UUID id, @Valid @RequestBody DiaryEntryDto request) {
        return diaryEntryService.create(id, request);
    }

    @GetMapping("/{id}/diaryEntry/{date}")
    @Operation(summary = "Get diary entry by project and date")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Diary entry found"),
            @ApiResponse(responseCode = "401", description = "Not authenticated",
                    content = @Content(schema = @Schema(implementation = ErrorDto.class))),
            @ApiResponse(responseCode = "404", description = "Diary entry or project not found",
                    content = @Content(schema = @Schema(implementation = ErrorDto.class)))
    })
    public DiaryEntryDto getDiaryEntry(@PathVariable UUID id, @PathVariable LocalDate date) {
        return diaryEntryService.getByProjectIdAndDate(id, date);
    }

    @GetMapping("/{id}/diaryEntries")
    @Operation(summary = "Get all diary entries for project with pagination")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Diary entries page"),
            @ApiResponse(responseCode = "401", description = "Not authenticated",
                    content = @Content(schema = @Schema(implementation = ErrorDto.class))),
            @ApiResponse(responseCode = "404", description = "Project not found",
                    content = @Content(schema = @Schema(implementation = ErrorDto.class)))
    })
    public Page<DiaryEntryDto> getAllDiaryEntries(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "0") int page) {
        return diaryEntryService.getAllByProjectId(id, PageRequest.of(page, 20));
    }
}