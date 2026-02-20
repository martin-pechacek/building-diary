package cz.mp.construction_site_diary.statemachine;

import cz.mp.construction_site_diary.entity.Project;
import cz.mp.construction_site_diary.statemachine.events.ProjectEvent;
import cz.mp.construction_site_diary.statemachine.states.ProjectStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.statemachine.ExtendedState;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.access.StateMachineAccess;
import org.springframework.statemachine.access.StateMachineAccessor;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.state.State;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ProjectStateMachineServiceTest {

    @Mock
    private StateMachineFactory<ProjectStatus, ProjectEvent> stateMachineFactory;

    @Mock
    private StateMachine<ProjectStatus, ProjectEvent> stateMachine;

    @Mock
    private StateMachineAccessor<ProjectStatus, ProjectEvent> accessor;

    @Mock
    private ExtendedState extendedState;

    @Mock
    private State<ProjectStatus, ProjectEvent> state;

    private ProjectStateMachineService service;

    @BeforeEach
    void setUp() {
        service = new ProjectStateMachineService(stateMachineFactory);
        setUpStateMachine();
    }

    void setUpStateMachine() {
        when(stateMachineFactory.getStateMachine(any(String.class))).thenReturn(stateMachine);
        when(stateMachine.stopReactively()).thenReturn(Mono.empty());
        when(stateMachine.startReactively()).thenReturn(Mono.empty());
        when(stateMachine.getStateMachineAccessor()).thenReturn(accessor);
        when(stateMachine.getExtendedState()).thenReturn(extendedState);
        when(extendedState.getVariables()).thenReturn(new HashMap<>());

        doAnswer(inv -> {
            java.util.function.Consumer<StateMachineAccess<ProjectStatus, ProjectEvent>> consumer = inv.getArgument(0);
            StateMachineAccess<ProjectStatus, ProjectEvent> accessMock = mock(StateMachineAccess.class);
            when(accessMock.resetStateMachineReactively(any())).thenReturn(Mono.empty());
            consumer.accept(accessMock);
            return null;
        }).when(accessor).doWithAllRegions(any());
    }

    @Nested
    class SendEvent {

        @Test
        void shouldTransitionFromPlanningToInProgressWhenStartWorkAccepted() {
            Project project = createProject(ProjectStatus.PLANNING);

            when(stateMachine.sendEvent(ProjectEvent.START_WORK)).thenReturn(true);
            when(stateMachine.getState()).thenReturn(state);
            when(state.getId()).thenReturn(ProjectStatus.IN_PROGRESS);

            boolean result = service.sendEvent(project, ProjectEvent.START_WORK);

            assertThat(result).isTrue();
            assertThat(project.getStatus()).isEqualTo(ProjectStatus.IN_PROGRESS);
            assertThat(project.getStartDate()).isEqualTo(LocalDate.now());
        }

        @Test
        void shouldTransitionFromInProgressToCompletedWhenCompleteAccepted() {
            Project project = createProject(ProjectStatus.IN_PROGRESS);
            project.setStartDate(LocalDate.now().minusDays(10));

            when(stateMachine.sendEvent(ProjectEvent.COMPLETE)).thenReturn(true);
            when(stateMachine.getState()).thenReturn(state);
            when(state.getId()).thenReturn(ProjectStatus.COMPLETED);

            boolean result = service.sendEvent(project, ProjectEvent.COMPLETE);

            assertThat(result).isTrue();
            assertThat(project.getStatus()).isEqualTo(ProjectStatus.COMPLETED);
            assertThat(project.getEndDate()).isEqualTo(LocalDate.now());
        }

        @Test
        void shouldReturnFalseWhenEventRejected() {
            Project project = createProject(ProjectStatus.PLANNING);

            when(stateMachine.sendEvent(ProjectEvent.START_WORK)).thenReturn(false);
            when(stateMachine.getState()).thenReturn(state);
            when(state.getId()).thenReturn(ProjectStatus.PLANNING); // State unchanged = rejected

            boolean result = service.sendEvent(project, ProjectEvent.START_WORK);

            assertThat(result).isFalse();
            assertThat(project.getStatus()).isEqualTo(ProjectStatus.PLANNING);
            assertThat(project.getStartDate()).isNull();
        }
    }

    private Project createProject(ProjectStatus status) {
        Project project = new Project();
        project.setId(UUID.randomUUID());
        project.setName("Test Project");
        project.setStatus(status);
        return project;
    }
}