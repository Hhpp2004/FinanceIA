package financa.backend.Config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.json.JsonMapper;

@Configuration
public class RabbitMqConfig {
    @Bean
    public Queue emailQueue(@Value("${broker.queue.email.name}") String queueName) {
        return new Queue(queueName, true);
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        JsonMapper jsonMapper = new JsonMapper();
        return new Jackson2JsonMessageConverter(jsonMapper);
    }
}
