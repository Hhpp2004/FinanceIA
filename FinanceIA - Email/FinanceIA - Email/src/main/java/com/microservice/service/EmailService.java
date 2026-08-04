package com.microservice.service;

import com.microservice.models.FinanceEmailSender;
import com.microservice.models.StatusEmail;
import com.microservice.repository.EmailRepository;
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
@Slf4j
public class EmailService {
    final EmailRepository emailRepository;
    final JavaMailSender javaMailSender;

    @Transactional
    public void sendEmail(FinanceEmailSender email, byte[] pdfBytes) {
        try {
            if (Boolean.TRUE.equals(email.getFilePDFExists())
                    && pdfBytes != null && pdfBytes.length > 0) {
                sendEmailWithPdf(email, pdfBytes);
            } else {
                sendSimpleEmail(email);
            }
            email.setStatus(StatusEmail.ENVIADO);
        } catch (MailException | MessagingException e) {
            email.setStatus(StatusEmail.ERRO);
            log.error("Erro ao enviar e-mail para {}", email.getEmailTo(), e);
        } finally {
            emailRepository.saveAndFlush(email);
            log.info("E-mail {} salvo com status {}", email.getId(), email.getStatus());
        }
    }

    private void sendSimpleEmail(FinanceEmailSender email) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email.getEmailTo());
        message.setSubject(email.getSubject());
        message.setText(email.getText());
        javaMailSender.send(message);
    }

    private void sendEmailWithPdf(FinanceEmailSender email, byte[] pdfBytes)
            throws MessagingException {
        var message = javaMailSender.createMimeMessage();
        var helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(email.getEmailTo());
        helper.setSubject(email.getSubject());
        helper.setText(email.getText());
        helper.addAttachment(
                "relatorio.pdf",
                new ByteArrayResource(pdfBytes),
                "application/pdf"
        );
        email.setFilePDFExists(true);
        javaMailSender.send(message);
    }

}
