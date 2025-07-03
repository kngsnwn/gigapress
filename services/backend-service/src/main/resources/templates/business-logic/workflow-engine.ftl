package ${packageName}.workflow;

import ${packageName}.entity.${entityName};
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ${entityName}WorkflowEngine {

    private final StateMachineFactory<${entityName}State, ${entityName}Event> stateMachineFactory;
    
    public void startWorkflow(${entityName} entity) {
        log.info("Starting workflow for ${entityName}: {}", entity.getId());
        
        StateMachine<${entityName}State, ${entityName}Event> stateMachine = 
            stateMachineFactory.getStateMachine(entity.getId().toString());
        
        stateMachine.start();
        stateMachine.getExtendedState().getVariables().put("entity", entity);
    }
    
    public void triggerEvent(Long entityId, ${entityName}Event event) {
        log.info("Triggering event {} for ${entityName}: {}", event, entityId);
        
        StateMachine<${entityName}State, ${entityName}Event> stateMachine = 
            stateMachineFactory.getStateMachine(entityId.toString());
        
        stateMachine.sendEvent(event);
    }
    
    public ${entityName}State getCurrentState(Long entityId) {
        StateMachine<${entityName}State, ${entityName}Event> stateMachine = 
            stateMachineFactory.getStateMachine(entityId.toString());
        
        return stateMachine.getState().getId();
    }
    
    public enum ${entityName}State {
        CREATED,
        IN_PROGRESS,
        UNDER_REVIEW,
        APPROVED,
        REJECTED,
        COMPLETED,
        ARCHIVED
    }
    
    public enum ${entityName}Event {
        START_PROCESSING,
        SUBMIT_FOR_REVIEW,
        APPROVE,
        REJECT,
        COMPLETE,
        ARCHIVE
    }
}
