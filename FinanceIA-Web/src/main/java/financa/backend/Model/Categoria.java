package financa.backend.Model;

import lombok.Getter;

@Getter
public enum Categoria {
    MORADIA(1l,"Moradia"),
    TRANSPORTE(2l,"Transporte"),
    INVESTIMENTOS(3l, "Investimentos"),
    LAZER(4l, "Lazer"),
    SAUDE(5l, "Saude"),
    OUTROS(6l, "Outros"),
    ALIMENTACAO(7l,"Alimentação"),
    IMPOSTOS(8l, "Impostos");

    private long id;
    private String nome;
    
    private Categoria(long id, String nome) {
        this.id = id;
        this.nome = nome;
    }
}
