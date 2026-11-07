package com.microservice.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
public class FinanceEmailSender  {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(name = "user_id")
    private int userId;
    private StatusEmail status;
    private String nome;
    private String code;
    @Value("${spring.mail.username}")
    private String emailFrom;
    private String emailTo;
    private String subject;
    @Column(columnDefinition = "TEXT")
    private String text;
    private LocalDateTime sendDateEmail = LocalDateTime.now();
    @Column(name = "file_pdf_exists")
    private Boolean filePDFExists = false;
}
