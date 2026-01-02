package cz.mp.building_diary.service.impl;

import cz.mp.building_diary.dto.AddressDto;
import cz.mp.building_diary.dto.ProjectDto;
import cz.mp.building_diary.entity.Address;
import cz.mp.building_diary.entity.Country;
import cz.mp.building_diary.entity.Project;
import cz.mp.building_diary.entity.User;
import cz.mp.building_diary.exception.ProjectNotFoundException;
import cz.mp.building_diary.exception.ProjectStateException;
import cz.mp.building_diary.exception.UserNotFoundException;
import cz.mp.building_diary.statemachine.events.ProjectEvent;
import cz.mp.building_diary.mapper.AddressMapper;
import cz.mp.building_diary.mapper.ProjectMapper;
import cz.mp.building_diary.repository.ProjectRepository;
import cz.mp.building_diary.repository.UserRepository;
import cz.mp.building_diary.service.SecurityService;
import cz.mp.building_diary.statemachine.ProjectStateMachineService;
import cz.mp.building_diary.statemachine.states.ProjectStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectServiceImplTest {

    private static final UUID PROJECT_ID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
    private static final UUID USER_ID = UUID.fromString("123e4567-e89b-12d3-a456-426614174001");
    private static final UUID MANAGER_ID = UUID.fromString("123e4567-e89b-12d3-a456-426614174002");

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProjectMapper projectMapper;

    @Mock
    private AddressMapper addressMapper;

    @Mock
    private ProjectStateMachineService stateMachineService;

    @Mock
    private SecurityService securityService;

    @InjectMocks
    private ProjectServiceImpl projectService;

    private User currentUser;
    private Project project;
    private ProjectDto projectDto;

    @BeforeEach
    void setUp() {
        currentUser = createUser(USER_ID, "user@example.com");
        project = createProject();
        projectDto = createProjectDto();
    }

    @Nested
    class Create {

        @Test
        void shouldCreateProjectSuccessfully() {
            when(securityService.getCurrentUser()).thenReturn(currentUser);
            when(projectMapper.toEntity(projectDto)).thenReturn(project);
            when(projectRepository.save(project)).thenReturn(project);
            when(projectMapper.toDto(project)).thenReturn(projectDto);

            ProjectDto result = projectService.create(projectDto);

            assertThat(result).isEqualTo(projectDto);
            assertThat(project.getCreatedBy()).isEqualTo(currentUser);
            assertThat(project.getStatus()).isEqualTo(ProjectStatus.PLANNING);
            verify(projectRepository).save(project);
        }
    }

    @Nested
    class GetById {

        @Test
        void shouldReturnProjectWhenFoundAndOwnedByCurrentUser() {
            when(projectRepository.findById(PROJECT_ID)).thenReturn(Optional.of(project));
            when(securityService.getCurrentUser()).thenReturn(currentUser);
            when(projectMapper.toDto(project)).thenReturn(projectDto);

            ProjectDto result = projectService.getById(PROJECT_ID);

            assertThat(result).isEqualTo(projectDto);
        }

        @Test
        void shouldThrowExceptionWhenProjectNotFound() {
            when(projectRepository.findById(PROJECT_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> projectService.getById(PROJECT_ID))
                    .isInstanceOf(ProjectNotFoundException.class)
                    .hasMessageContaining(PROJECT_ID.toString());
        }

        @Test
        void shouldThrowExceptionWhenProjectNotOwnedByCurrentUser() {
            User otherUser = createUser(UUID.randomUUID(), "other@example.com");
            project.setCreatedBy(otherUser);

            when(projectRepository.findById(PROJECT_ID)).thenReturn(Optional.of(project));
            when(securityService.getCurrentUser()).thenReturn(currentUser);

            assertThatThrownBy(() -> projectService.getById(PROJECT_ID))
                    .isInstanceOf(ProjectNotFoundException.class);
        }
    }

    @Nested
    class GetAll {

        @Test
        void shouldReturnAllProjectsForCurrentUser() {
            when(securityService.getCurrentUser()).thenReturn(currentUser);
            when(projectRepository.findByCreatedById(USER_ID)).thenReturn(List.of(project));
            when(projectMapper.toDto(project)).thenReturn(projectDto);

            List<ProjectDto> result = projectService.getAll();

            assertThat(result).hasSize(1);
            assertThat(result.get(0)).isEqualTo(projectDto);
        }

        @Test
        void shouldReturnEmptyListWhenNoProjects() {
            when(securityService.getCurrentUser()).thenReturn(currentUser);
            when(projectRepository.findByCreatedById(USER_ID)).thenReturn(List.of());

            List<ProjectDto> result = projectService.getAll();

            assertThat(result).isEmpty();
        }
    }

    @Nested
    class Update {

        @Test
        void shouldUpdateProjectSuccessfully() {
            when(projectRepository.findById(PROJECT_ID)).thenReturn(Optional.of(project));
            when(securityService.getCurrentUser()).thenReturn(currentUser);
            when(projectRepository.save(project)).thenReturn(project);
            when(projectMapper.toDto(project)).thenReturn(projectDto);

            ProjectDto result = projectService.update(PROJECT_ID, projectDto);

            assertThat(result).isEqualTo(projectDto);
            verify(projectMapper).updateFromDto(projectDto, project);
            verify(projectRepository).save(project);
        }

        @Test
        void shouldUpdateAddressWhenProvided() {
            Address address = new Address();
            project.setConstructionSiteAddress(address);
            AddressDto addressDto = new AddressDto("1234/5", null, null, "Praha", "11000", Country.CZ);
            ProjectDto dtoWithAddress = new ProjectDto(
                    PROJECT_ID, "Test", null, null, addressDto, null,
                    null, null, null, null, null, null
            );

            when(projectRepository.findById(PROJECT_ID)).thenReturn(Optional.of(project));
            when(securityService.getCurrentUser()).thenReturn(currentUser);
            when(projectRepository.save(project)).thenReturn(project);
            when(projectMapper.toDto(project)).thenReturn(dtoWithAddress);

            projectService.update(PROJECT_ID, dtoWithAddress);

            verify(addressMapper).updateFromDto(addressDto, address);
        }

        @Test
        void shouldUpdateConstructionManagerWhenProvided() {
            User manager = createUser(MANAGER_ID, "manager@example.com");
            ProjectDto dtoWithManager = new ProjectDto(
                    PROJECT_ID, "Test", null, null, null, null,
                    null, MANAGER_ID, null, null, null, null
            );

            when(projectRepository.findById(PROJECT_ID)).thenReturn(Optional.of(project));
            when(securityService.getCurrentUser()).thenReturn(currentUser);
            when(userRepository.findById(MANAGER_ID)).thenReturn(Optional.of(manager));
            when(projectRepository.save(project)).thenReturn(project);
            when(projectMapper.toDto(project)).thenReturn(dtoWithManager);

            projectService.update(PROJECT_ID, dtoWithManager);

            assertThat(project.getConstructionManager()).isEqualTo(manager);
        }

        @Test
        void shouldThrowExceptionWhenConstructionManagerNotFound() {
            ProjectDto dtoWithManager = new ProjectDto(
                    PROJECT_ID, "Test", null, null, null, null,
                    null, MANAGER_ID, null, null, null, null
            );

            when(projectRepository.findById(PROJECT_ID)).thenReturn(Optional.of(project));
            when(securityService.getCurrentUser()).thenReturn(currentUser);
            when(userRepository.findById(MANAGER_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> projectService.update(PROJECT_ID, dtoWithManager))
                    .isInstanceOf(UserNotFoundException.class);
        }
    }

    @Nested
    class Archive {

        @Test
        void shouldArchiveProjectSuccessfully() {
            when(projectRepository.findById(PROJECT_ID)).thenReturn(Optional.of(project));
            when(securityService.getCurrentUser()).thenReturn(currentUser);

            projectService.archive(PROJECT_ID);

            assertThat(project.isArchived()).isTrue();
            verify(projectRepository).save(project);
        }

        @Test
        void shouldThrowExceptionWhenProjectNotFound() {
            when(projectRepository.findById(PROJECT_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> projectService.archive(PROJECT_ID))
                    .isInstanceOf(ProjectNotFoundException.class);
        }
    }

    @Nested
    class Start {

        @Test
        void shouldStartProjectWhenTransitionAccepted() {
            when(projectRepository.findById(PROJECT_ID)).thenReturn(Optional.of(project));
            when(securityService.getCurrentUser()).thenReturn(currentUser);
            when(stateMachineService.sendEvent(project, ProjectEvent.START_WORK)).thenReturn(true);
            when(projectRepository.save(project)).thenReturn(project);
            when(projectMapper.toDto(project)).thenReturn(projectDto);

            ProjectDto result = projectService.start(PROJECT_ID);

            assertThat(result).isEqualTo(projectDto);
            verify(stateMachineService).sendEvent(project, ProjectEvent.START_WORK);
            verify(projectRepository).save(project);
        }

        @Test
        void shouldThrowExceptionWhenTransitionRejected() {
            when(projectRepository.findById(PROJECT_ID)).thenReturn(Optional.of(project));
            when(securityService.getCurrentUser()).thenReturn(currentUser);
            when(stateMachineService.sendEvent(project, ProjectEvent.START_WORK)).thenReturn(false);

            assertThatThrownBy(() -> projectService.start(PROJECT_ID))
                    .isInstanceOf(ProjectStateException.class)
                    .hasMessageContaining("Cannot start project");
        }
    }

    @Nested
    class Complete {

        @Test
        void shouldCompleteProjectWhenTransitionAccepted() {
            when(projectRepository.findById(PROJECT_ID)).thenReturn(Optional.of(project));
            when(securityService.getCurrentUser()).thenReturn(currentUser);
            when(stateMachineService.sendEvent(project, ProjectEvent.COMPLETE)).thenReturn(true);
            when(projectRepository.save(project)).thenReturn(project);
            when(projectMapper.toDto(project)).thenReturn(projectDto);

            ProjectDto result = projectService.complete(PROJECT_ID);

            assertThat(result).isEqualTo(projectDto);
            verify(stateMachineService).sendEvent(project, ProjectEvent.COMPLETE);
            verify(projectRepository).save(project);
        }

        @Test
        void shouldThrowExceptionWhenTransitionRejected() {
            when(projectRepository.findById(PROJECT_ID)).thenReturn(Optional.of(project));
            when(securityService.getCurrentUser()).thenReturn(currentUser);
            when(stateMachineService.sendEvent(project, ProjectEvent.COMPLETE)).thenReturn(false);

            assertThatThrownBy(() -> projectService.complete(PROJECT_ID))
                    .isInstanceOf(ProjectStateException.class)
                    .hasMessageContaining("Cannot complete project");
        }
    }

    private User createUser(UUID id, String email) {
        User user = new User("kc-" + id, email, "Test", "User");
        user.setId(id);
        return user;
    }

    private Project createProject() {
        Project p = new Project();
        p.setId(PROJECT_ID);
        p.setName("Test Project");
        p.setStatus(ProjectStatus.PLANNING);
        p.setCreatedBy(currentUser);
        return p;
    }

    private ProjectDto createProjectDto() {
        return new ProjectDto(
                PROJECT_ID,
                "Test Project",
                null,
                null,
                null,
                ProjectStatus.PLANNING,
                null,
                null,
                null,
                null,
                null,
                null
        );
    }
}