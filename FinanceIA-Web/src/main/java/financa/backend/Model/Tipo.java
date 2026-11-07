package financa.backend.Model;

import lombok.Getter;

@Getter
public enum Tipo {
    ENTRADA(1l,"Entrada"),
    SAIDA(2l,"Saida");

    private long id;
    private String nome;

    private Tipo(long id, String nome) {
        this.id = id;
        this.nome = nome;
    }
}