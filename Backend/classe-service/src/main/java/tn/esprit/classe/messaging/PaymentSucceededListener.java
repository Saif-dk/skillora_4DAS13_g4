package tn.esprit.classe.messaging;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import tn.esprit.classe.config.RabbitMQConfig;
import tn.esprit.classe.dto.PaymentSucceededEvent;
import tn.esprit.classe.service.PaymentEventService;

@Component
@RequiredArgsConstructor
public class PaymentSucceededListener {

    private final PaymentEventService paymentEventService;

    @RabbitListener(queues = RabbitMQConfig.QUEUE)
    public void onPaymentSucceeded(PaymentSucceededEvent event) {
        paymentEventService.process(event);
    }
}
