package ${packageName}.notification;

import ${packageName}.entity.${entityName};
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ${entityName}NotificationService {

    private final JavaMailSender mailSender;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String NOTIFICATION_TOPIC = "${entityName?lower_case}-notifications";

    public void sendEmailNotification(${entityName} entity, String recipientEmail, NotificationType type) {
        log.info("Sending email notification for ${entityName}: {} to {}", entity.getId(), recipientEmail);
        
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(recipientEmail);
            message.setSubject(getEmailSubject(type, entity));
            message.setText(getEmailBody(type, entity));
            
            mailSender.send(message);
            
            // Send event to Kafka
            publishNotificationEvent(entity, type, "EMAIL", recipientEmail);
            
        } catch (Exception e) {
            log.error("Failed to send email notification", e);
            throw new RuntimeException("Email notification failed", e);
        }
    }
    
    public void sendSmsNotification(${entityName} entity, String phoneNumber, NotificationType type) {
        log.info("Sending SMS notification for ${entityName}: {} to {}", entity.getId(), phoneNumber);
        
        // SMS implementation would go here
        // For now, just publish to Kafka
        publishNotificationEvent(entity, type, "SMS", phoneNumber);
    }
    
    public void sendPushNotification(${entityName} entity, String userId, NotificationType type) {
        log.info("Sending push notification for ${entityName}: {} to user {}", entity.getId(), userId);
        
        // Push notification implementation would go here
        publishNotificationEvent(entity, type, "PUSH", userId);
    }
    
    private void publishNotificationEvent(${entityName} entity, NotificationType type, 
                                         String channel, String recipient) {
        Map<String, Object> event = new HashMap<>();
        event.put("entityId", entity.getId());
        event.put("entityType", "${entityName}");
        event.put("notificationType", type);
        event.put("channel", channel);
        event.put("recipient", recipient);
        event.put("timestamp", System.currentTimeMillis());
        
        kafkaTemplate.send(NOTIFICATION_TOPIC, event);
    }
    
    private String getEmailSubject(NotificationType type, ${entityName} entity) {
        return switch (type) {
            case CREATED -> "${entityName} Created: " + entity.getId();
            case UPDATED -> "${entityName} Updated: " + entity.getId();
            case DELETED -> "${entityName} Deleted: " + entity.getId();
            case STATUS_CHANGED -> "${entityName} Status Changed: " + entity.getId();
            default -> "${entityName} Notification: " + entity.getId();
        };
    }
    
    private String getEmailBody(NotificationType type, ${entityName} entity) {
        StringBuilder body = new StringBuilder();
        body.append("Dear User,\n\n");
        
        switch (type) {
            case CREATED:
                body.append("A new ${entityName} has been created.\n");
                break;
            case UPDATED:
                body.append("${entityName} has been updated.\n");
                break;
            case DELETED:
                body.append("${entityName} has been deleted.\n");
                break;
            case STATUS_CHANGED:
                body.append("${entityName} status has changed.\n");
                break;
        }
        
        body.append("\nDetails:\n");
        body.append("ID: ").append(entity.getId()).append("\n");
<#list fields as field>
        body.append("${field.name?cap_first}: ").append(entity.get${field.name?cap_first}()).append("\n");
</#list>
        
        body.append("\nBest regards,\n${entityName} System");
        
        return body.toString();
    }
    
    public enum NotificationType {
        CREATED,
        UPDATED,
        DELETED,
        STATUS_CHANGED,
        CUSTOM
    }
}
