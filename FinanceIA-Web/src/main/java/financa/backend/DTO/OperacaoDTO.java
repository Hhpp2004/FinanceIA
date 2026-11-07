package financa.backend.DTO;

import financa.backend.Model.Categoria;
import financa.backend.Model.Tipo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record OperacaoDTO(
        @NotBlank String nome,
        @NotBlank String descricao,
        @NotNull  Double valor,
        @NotNull String data,
        Categoria categoria,
        @NotNull Tipo tipo) {
    
}
