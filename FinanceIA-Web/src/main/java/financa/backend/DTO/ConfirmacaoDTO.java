package financa.backend.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ConfirmacaoDTO(@Email @NotBlank String email, @NotBlank String codigo) {

}
