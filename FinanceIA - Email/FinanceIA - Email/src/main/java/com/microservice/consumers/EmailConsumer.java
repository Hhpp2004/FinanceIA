package com.microservice.consumers;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.BeanUtils;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import com.microservice.dtos.EmailDto;
import com.microservice.models.FinanceEmailSender;
import com.microservice.service.EmailService;

import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class EmailConsumer {
    final EmailService es;

    @RabbitListener(queues = "${broker.queue.email.name}")
    public void listenEmailQueue(@Payload EmailDto emailDto){
        FinanceEmailSender newEmail = new FinanceEmailSender();
        BeanUtils.copyProperties(emailDto, newEmail);
        newEmail.setFilePDFExists(emailDto.filePDFExists());
        es.sendEmail(newEmail, emailDto.filePDF());
    }
}
