package financa.backend.Model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor
@Setter
@Entity(name = "operacao_agendado")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Agendamento {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private LocalDate data;
    @JoinColumn(name = "valor_agendado")
    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Saldo saldo = new Saldo();
    @JoinColumn(name = "lista")
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private List<Operacao> operacoes = new ArrayList<>();
    
    public Agendamento(LocalDate data, Saldo saldo, List<Operacao> operacoes) {
        this.data = data;
        this.saldo = saldo;
        this.operacoes = operacoes;
    }
}
