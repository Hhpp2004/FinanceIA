package financa.backend.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;


public record CreateUserDTO(@NotBlank(message = "Nome: campo Obrigatorio") String nome, @NotBlank(message = "Telefone: campo Obrigatorio") String telefone, @NotBlank(message = "Senha: campo Obrigatorio") String senha, @Email @NotNull(message = "Email: campo Obrigatorio") String email) {
}
