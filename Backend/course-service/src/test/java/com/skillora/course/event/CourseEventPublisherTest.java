package com.skillora.course.event;

import com.skillora.course.config.RabbitMQConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.time.LocalDateTime;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class CourseEventPublisherTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Test
    void publishCourseCreatedSendsEventToExchange() {
        CourseEventPublisher publisher = new CourseEventPublisher(rabbitTemplate, true);
        CourseCreatedEvent event = new CourseCreatedEvent(
                "evt-1",
                10L,
                "Java",
                55L,
                LocalDateTime.of(2026, 11, 24, 10, 0));

        publisher.publishCourseCreated(event);

        verify(rabbitTemplate).convertAndSend(
                RabbitMQConfig.SKILLORA_EXCHANGE,
                RabbitMQConfig.COURSE_CREATED_ROUTING_KEY,
                event);
    }

    @Test
    void publishCourseCreatedSkipsEventWhenDisabled() {
        CourseEventPublisher publisher = new CourseEventPublisher(rabbitTemplate, false);
        CourseCreatedEvent event = new CourseCreatedEvent(
                "evt-2",
                11L,
                "Angular",
                55L,
                LocalDateTime.of(2026, 11, 24, 10, 1));

        publisher.publishCourseCreated(event);

        verifyNoInteractions(rabbitTemplate);
    }
}
