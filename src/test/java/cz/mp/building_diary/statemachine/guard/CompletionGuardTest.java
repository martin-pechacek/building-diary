package cz.mp.building_diary.statemachine.guard;

import cz.mp.building_diary.entity.Project;
import cz.mp.building_diary.statemachine.config.ProjectStateMachineConfig;
import cz.mp.building_diary.statemachine.events.ProjectEvent;
import cz.mp.building_diary.statemachine.states.ProjectStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.statemachine.ExtendedState;
import org.springframework.statemachine.StateContext;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CompletionGuardTest {

    private CompletionGuard guard;
    private StateContext<ProjectStatus, ProjectEvent> context;
    private ExtendedState extendedState;

    @BeforeEach
    void setUp() {
        guard = new CompletionGuard();
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
    class WhenProjectHasNoStartDate {

        @Test
        void shouldReturnFalse() {
            Project project = createProject();
            project.setStartDate(null);
            when(extendedState.get(ProjectStateMachineConfig.PROJECT_HEADER, Project.class)).thenReturn(project);

            assertThat(guard.evaluate(context)).isFalse();
        }
    }

    @Nested
    class WhenDiaryEntriesNotFilled {

        @Test
        void shouldReturnFalseWhenNoDiaryEntriesFilled() {
            Project project = createProject();
            project.setStartDate(LocalDate.now().minusDays(5));
            when(extendedState.get(ProjectStateMachineConfig.PROJECT_HEADER, Project.class)).thenReturn(project);

            // Currently returns false because filledDays is hardcoded to 0 (TODO in guard)
            assertThat(guard.evaluate(context)).isFalse();
        }
    }

    private Project createProject() {
        Project project = new Project();
        project.setId(UUID.randomUUID());
        project.setName("Test Project");
        project.setStatus(ProjectStatus.IN_PROGRESS);
        return project;
    }
}