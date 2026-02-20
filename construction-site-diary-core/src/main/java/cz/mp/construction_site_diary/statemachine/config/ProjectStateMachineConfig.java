package cz.mp.construction_site_diary.statemachine.config;

import cz.mp.construction_site_diary.statemachine.events.ProjectEvent;
import cz.mp.construction_site_diary.statemachine.states.ProjectStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.guard.Guard;

import java.util.EnumSet;

@Configuration
@EnableStateMachineFactory
@RequiredArgsConstructor
public class ProjectStateMachineConfig extends EnumStateMachineConfigurerAdapter<ProjectStatus, ProjectEvent> {

    public static final String PROJECT_HEADER = "project";

    private final Guard<ProjectStatus, ProjectEvent> startWorkGuard;
    private final Guard<ProjectStatus, ProjectEvent> completionGuard;

    @Override
    public void configure(StateMachineStateConfigurer<ProjectStatus, ProjectEvent> states) throws Exception {
        states
                .withStates()
                .initial(ProjectStatus.PLANNING)
                .end(ProjectStatus.COMPLETED)
                .states(EnumSet.allOf(ProjectStatus.class));
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<ProjectStatus, ProjectEvent> transitions) throws Exception {
        transitions
                .withExternal()
                .source(ProjectStatus.PLANNING)
                .target(ProjectStatus.IN_PROGRESS)
                .event(ProjectEvent.START_WORK)
                .guard(startWorkGuard)
                .and()
                .withExternal()
                .source(ProjectStatus.IN_PROGRESS)
                .target(ProjectStatus.COMPLETED)
                .event(ProjectEvent.COMPLETE)
                .guard(completionGuard);
    }
}