package com.skillora.course.event;

import com.skillora.course.config.RabbitMQConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class CourseEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(CourseEventPublisher.class);

    private final RabbitTemplate rabbitTemplate;

    public CourseEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishCourseCreated(CourseCreatedEvent event) {
        log.info("Publishing CourseCreatedEvent eventId={} courseId={}", event.eventId(), event.courseId());
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.SKILLORA_EXCHANGE,
                RabbitMQConfig.COURSE_CREATED_ROUTING_KEY,
                event);
    }
}
