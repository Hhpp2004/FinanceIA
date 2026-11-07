package com.microservice.models;

import lombok.Getter;

@Getter
public enum StatusEmail{
    ENVIADO(1L,"Enviado"),
    ERRO(2L,"Erro");

    private long id;
    private String name;

    StatusEmail(long id, String name) {
        this.id = id;
        this.name = name;
    }
}