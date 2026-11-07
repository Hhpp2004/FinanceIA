package financa.backend.DTO;

import jakarta.validation.constraints.NotBlank;

public record GeradorPdfDTO(@NotBlank String mes) {
    
}
