package cz.mp.construction_site_diary.controller;

import cz.mp.construction_site_diary.dto.ErrorDto;
import cz.mp.construction_site_diary.dto.ProjectDto;
import cz.mp.construction_site_diary.service.DiaryExportService;
import cz.mp.construction_site_diary.service.ProjectService;
import cz.mp.construction_site_diary.enums.ExportFormat;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(ProjectController.URL)
@Tag(name = "Projects", description = "Project management endpoints")
@RequiredArgsConstructor
public class ProjectController extends BaseController {

    public static final String URL = BASE_PATH + "/projects";

    private final ProjectService projectService;
    private final DiaryExportService diaryExportService;

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

    @GetMapping("/{id}/export/{format}")
    @Operation(summary = "Export project diary to specified format (csv, pdf)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Export file generated"),
            @ApiResponse(responseCode = "401", description = "Not authenticated",
                    content = @Content(schema = @Schema(implementation = ErrorDto.class))),
            @ApiResponse(responseCode = "404", description = "Project not found",
                    content = @Content(schema = @Schema(implementation = ErrorDto.class))),
            @ApiResponse(responseCode = "500", description = "Export failed",
                    content = @Content(schema = @Schema(implementation = ErrorDto.class)))
    })
    public ResponseEntity<byte[]> export(@PathVariable UUID id, @PathVariable String format) {
        ExportFormat exportFormat = ExportFormat.valueOf(format.toUpperCase());
        byte[] data = diaryExportService.export(id, exportFormat);
        MediaType contentType = exportFormat == ExportFormat.PDF ? MediaType.APPLICATION_PDF : MediaType.parseMediaType("text/csv");

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"diary-export-" + id + "." + format.toLowerCase() + "\"")
                .contentType(contentType)
                .body(data);
    }
}