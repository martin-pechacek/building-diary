package cz.mp.construction_site_diary.service.impl;

import cz.mp.construction_site_diary.dto.ProjectDto;
import cz.mp.construction_site_diary.entity.Project;
import cz.mp.construction_site_diary.entity.User;
import cz.mp.construction_site_diary.event.ProjectStatusChangedEvent;
import cz.mp.construction_site_diary.exception.ProjectNotFoundException;
import cz.mp.construction_site_diary.exception.ProjectStateException;
import cz.mp.construction_site_diary.exception.UserNotFoundException;
import cz.mp.construction_site_diary.mapper.AddressMapper;
import cz.mp.construction_site_diary.mapper.ProjectMapper;
import cz.mp.construction_site_diary.repository.ProjectRepository;
import cz.mp.construction_site_diary.repository.UserRepository;
import cz.mp.construction_site_diary.service.ProjectService;
import cz.mp.construction_site_diary.service.SecurityService;
import cz.mp.construction_site_diary.statemachine.ProjectStateMachineService;
import cz.mp.construction_site_diary.statemachine.events.ProjectEvent;
import cz.mp.construction_site_diary.statemachine.states.ProjectStatus;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private static final Logger LOG = LoggerFactory.getLogger(ProjectServiceImpl.class);

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ProjectMapper projectMapper;
    private final AddressMapper addressMapper;
    private final ProjectStateMachineService stateMachineService;
    private final SecurityService securityService;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    @CacheEvict(value = "projectsByUser", key = "@securityService.getCurrentUser().id")
    public ProjectDto create(ProjectDto dto) {
        User currentUser = securityService.getCurrentUser();

        Project project = projectMapper.toEntity(dto);
        project.setCreatedBy(currentUser);
        project.setStatus(ProjectStatus.PLANNING);

        Project savedProject = projectRepository.save(project);
        LOG.info("Project created: {} by user: {}", savedProject.getId(), currentUser.getEmail());

        return projectMapper.toDto(savedProject);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "projects", key = "#id")
    public ProjectDto getById(UUID id) {
        return projectMapper.toDto(findProjectById(id));
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(
            value = "projectsByUser",
            key = "@securityService.getCurrentUser().id"
    )
    public List<ProjectDto> getAll() {
        if (securityService.isAdmin()) {
            return projectRepository.findAll().stream()
                    .map(projectMapper::toDto)
                    .toList();
        }
        User currentUser = securityService.getCurrentUser();
        return projectRepository.findByCreatedById(currentUser.getId()).stream()
                .map(projectMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "projects", key = "#id"),
            @CacheEvict(value = "projectsByUser", key = "@securityService.getCurrentUser().id")
    })
    public ProjectDto update(UUID id, ProjectDto dto) {
        Project project = findProjectById(id);

        projectMapper.updateFromDto(dto, project);

        Optional.ofNullable(dto.constructionSiteAddress())
                .ifPresent(addressDto ->
                        addressMapper.updateFromDto(addressDto, project.getConstructionSiteAddress())
                );

        Optional.ofNullable(dto.constructionManagerId())
                .map(this::findUserById)
                .ifPresent(project::setConstructionManager);

        Project savedProject = projectRepository.save(project);
        LOG.info("Project updated: {}", savedProject.getId());

        return projectMapper.toDto(savedProject);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "projects", key = "#id"),
            @CacheEvict(value = "projectsByUser", key = "@securityService.getCurrentUser().id")
    })
    public void archive(UUID id) {
        Project project = findProjectById(id);
        project.setArchived(true);
        Project savedProject = projectRepository.save(project);
        LOG.info("Project archived: {}", savedProject.getId());
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "projects", key = "#id"),
            @CacheEvict(value = "projectsByUser", key = "@securityService.getCurrentUser().id")
    })
    public ProjectDto start(UUID id) {
        Project project = findProjectById(id);

        if (!stateMachineService.sendEvent(project, ProjectEvent.START_WORK)) {
            throw new ProjectStateException("Cannot start project. Ensure construction manager, address, and permit are set.");
        }

        Project savedProject = projectRepository.save(project);

        eventPublisher.publishEvent(new ProjectStatusChangedEvent(
                savedProject.getId().toString(),
                savedProject.getName(),
                savedProject.getCreatedBy().getEmail(),
                savedProject.getConstructionManager() != null ? savedProject.getConstructionManager().getEmail() : null,
                savedProject.getStatus().name(),
                savedProject.getStartDate()
        ));

        return projectMapper.toDto(savedProject);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "projects", key = "#id"),
            @CacheEvict(value = "projectsByUser", key = "@securityService.getCurrentUser().id")
    })
    public ProjectDto complete(UUID id) {
        Project project = findProjectById(id);

        if (!stateMachineService.sendEvent(project, ProjectEvent.COMPLETE)) {
            throw new ProjectStateException("Cannot complete project. Ensure all diary entries are filled.");
        }

        Project savedProject = projectRepository.save(project);

        eventPublisher.publishEvent(new ProjectStatusChangedEvent(
                savedProject.getId().toString(),
                savedProject.getName(),
                savedProject.getCreatedBy().getEmail(),
                savedProject.getConstructionManager() != null ? savedProject.getConstructionManager().getEmail() : null,
                savedProject.getStatus().name(),
                savedProject.getEndDate()
        ));

        return projectMapper.toDto(savedProject);
    }

    private Project findProjectById(UUID id) {
        hasAccess(id);
        return projectRepository.findById(id)
                .orElseThrow(() -> new ProjectNotFoundException("Project not found: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public void hasAccess(UUID id) {
        if (securityService.isAdmin()) {
            return;
        }
        User currentUser = securityService.getCurrentUser();
        if (!projectRepository.hasAccess(id, currentUser.getId())) {
            throw new ProjectNotFoundException("Project not found: " + id);
        }
    }

    private User findUserById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + id));
    }
}