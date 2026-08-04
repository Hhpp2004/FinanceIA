package financa.backend.DTO;

import jakarta.validation.constraints.NotBlank;

public record SendFileAutoDTO(@NotBlank String dia) {
    
}
