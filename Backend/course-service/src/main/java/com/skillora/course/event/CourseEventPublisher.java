package com.skillora.course.event;

import com.skillora.course.config.RabbitMQConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CourseEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(CourseEventPublisher.class);

    private final RabbitTemplate rabbitTemplate;
    private final boolean enabled;

    public CourseEventPublisher(
            RabbitTemplate rabbitTemplate,
            @Value("${skillora.events.enabled:true}") boolean enabled) {
        this.rabbitTemplate = rabbitTemplate;
        this.enabled = enabled;
    }

    public void publishCourseCreated(CourseCreatedEvent event) {
        if (!enabled) {
            log.info("CourseCreatedEvent non publié (événements désactivés) eventId={} courseId={}", event.eventId(), event.courseId());
            return;
        }

        log.info("Publishing CourseCreatedEvent eventId={} courseId={}", event.eventId(), event.courseId());
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.SKILLORA_EXCHANGE,
                    RabbitMQConfig.COURSE_CREATED_ROUTING_KEY,
                    event);
        } catch (AmqpException exception) {
            log.warn("RabbitMQ indisponible, CourseCreatedEvent non publié eventId={} courseId={}", event.eventId(), event.courseId());
        }
    }
}
