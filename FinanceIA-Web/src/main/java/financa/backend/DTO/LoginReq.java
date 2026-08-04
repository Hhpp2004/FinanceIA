package financa.backend.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginReq(@NotBlank(message = "Senha: campo obrigatorio") String senha, @Email @NotBlank(message = "Email: campo obrigatorio") String email) {

}
