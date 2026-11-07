package financa.backend.Producer;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import financa.backend.DTO.EmailDTO;

@Component
public class FinancerProducer { 
    private final RabbitTemplate rabbitTemplate;

    public FinancerProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Value("${broker.queue.email.name}")
    private String broker;

    public void publishMessageEmail(EmailDTO email) {
        rabbitTemplate.convertAndSend("",broker,email);
    }
}
