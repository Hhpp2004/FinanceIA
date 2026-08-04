package com.microservice.dtos;

import com.fasterxml.jackson.annotation.JsonAlias;

public record EmailDto(
        int userId,
        String emailTo,
        String subject,
        String text,
        String nome,
        String code,
        byte[] filePDF,
        @JsonAlias("isFilePDFExists") boolean filePDFExists
){}
