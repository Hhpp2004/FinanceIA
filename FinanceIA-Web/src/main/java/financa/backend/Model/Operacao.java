package financa.backend.Model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Operacao {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private String nome;
    private String descricao;
    private Double valor;
    private LocalDate data;
    private LocalTime time;
    @Enumerated(EnumType.STRING)
    private Categoria categoria;
    @Enumerated(EnumType.STRING)
    private Tipo tipo;
    @ManyToOne
    @JsonIgnore
    private Extrato extrato;
    private Boolean isAgendado;

    private LocalDate dataFormater(String data) {
        if (data == null || data.isBlank()) {
            throw new IllegalArgumentException("Data da operacao invalida");
        }

        List<DateTimeFormatter> formatters = List.of(
                DateTimeFormatter.ofPattern("dd/MMMM/yyyy", Locale.of("pt", "BR")),
                DateTimeFormatter.ofPattern("dd-MM-yyyy"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd"),
                DateTimeFormatter.ofPattern("dd/MM/yyyy"));

        for (DateTimeFormatter formatter : formatters) {
            try {
                return LocalDate.parse(data.trim(), formatter);
            } catch (DateTimeParseException ignored) {
            }
        }

        throw new IllegalArgumentException("Formato de data da operacao invalido: " + data);
    }

    public Operacao(String nome, String descricao, Double valor, String data, Categoria categoria,
            Tipo tipo, Extrato extrato,boolean isAgendado) {
        this.nome = nome;
        this.descricao = descricao;
        this.valor = valor;
        this.data = dataFormater(data);
        this.time = LocalTime.now();
        this.categoria = categoria;
        this.tipo = tipo;
        this.extrato = extrato;
        this.isAgendado = isAgendado;
    }
}
