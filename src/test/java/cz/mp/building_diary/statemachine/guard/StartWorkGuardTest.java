package cz.mp.building_diary.statemachine.guard;

import cz.mp.building_diary.entity.Address;
import cz.mp.building_diary.entity.Country;
import cz.mp.building_diary.entity.Project;
import cz.mp.building_diary.entity.User;
import cz.mp.building_diary.statemachine.config.ProjectStateMachineConfig;
import cz.mp.building_diary.statemachine.events.ProjectEvent;
import cz.mp.building_diary.statemachine.states.ProjectStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.statemachine.ExtendedState;
import org.springframework.statemachine.StateContext;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class StartWorkGuardTest {

    private StartWorkGuard guard;
    private StateContext<ProjectStatus, ProjectEvent> context;
    private ExtendedState extendedState;

    @BeforeEach
    void setUp() {
        guard = new StartWorkGuard();
        context = mock(StateContext.class);
        extendedState = mock(ExtendedState.class);

        when(context.getExtendedState()).thenReturn(extendedState);
    }

    @Nested
    class WhenProjectIsNull {

        @Test
        void shouldReturnFalse() {
            when(extendedState.get(ProjectStateMachineConfig.PROJECT_HEADER, Project.class)).thenReturn(null);

            assertThat(guard.evaluate(context)).isFalse();
        }
    }

    @Nested
    class WhenProjectHasAllRequirements {

        @Test
        void shouldReturnTrue() {
            Project project = createValidProject();
            when(extendedState.get(ProjectStateMachineConfig.PROJECT_HEADER, Project.class)).thenReturn(project);

            assertThat(guard.evaluate(context)).isTrue();
        }
    }

    @Nested
    class WhenProjectIsMissingRequirements {

        @Test
        void shouldReturnFalseWhenMissingConstructionManager() {
            Project project = createValidProject();
            project.setConstructionManager(null);
            when(extendedState.get(ProjectStateMachineConfig.PROJECT_HEADER, Project.class)).thenReturn(project);

            assertThat(guard.evaluate(context)).isFalse();
        }

        @Test
        void shouldReturnFalseWhenMissingAddress() {
            Project project = createValidProject();
            project.setConstructionSiteAddress(null);
            when(extendedState.get(ProjectStateMachineConfig.PROJECT_HEADER, Project.class)).thenReturn(project);

            assertThat(guard.evaluate(context)).isFalse();
        }

        @Test
        void shouldReturnFalseWhenMissingBuildingPermit() {
            Project project = createValidProject();
            project.setBuildingPermitNumber(null);
            when(extendedState.get(ProjectStateMachineConfig.PROJECT_HEADER, Project.class)).thenReturn(project);

            assertThat(guard.evaluate(context)).isFalse();
        }

        @Test
        void shouldReturnFalseWhenBuildingPermitIsBlank() {
            Project project = createValidProject();
            project.setBuildingPermitNumber("   ");
            when(extendedState.get(ProjectStateMachineConfig.PROJECT_HEADER, Project.class)).thenReturn(project);

            assertThat(guard.evaluate(context)).isFalse();
        }
    }

    private Project createValidProject() {
        Project project = new Project();
        project.setId(UUID.randomUUID());
        project.setName("Test Project");
        project.setStatus(ProjectStatus.PLANNING);
        project.setBuildingPermitNumber("BP-2024-001");

        User manager = new User("kc-123", "manager@example.com", "Manager", "User");
        project.setConstructionManager(manager);

        Address address = new Address();
        address.setParcelNumber("1234/5");
        address.setCity("Praha");
        address.setPostalCode("11000");
        address.setCountry(Country.CZ);
        project.setConstructionSiteAddress(address);

        return project;
    }
}