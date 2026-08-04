package financa.backend.Model;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Saldo {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID saldo;
    private double saldoAtual;
    private double saldoEntrada;
    private double saldoSaida;
    
    public Saldo() {
        this.saldoAtual = 0.00;
        this.saldoEntrada = 0.00;
        this.saldoSaida = 0.00;
    }

    public Saldo(double saldoAtual, double saldoEntrada, double saldoSaida) {
        this.saldoAtual = saldoAtual;
        this.saldoEntrada = saldoEntrada;
        this.saldoSaida = saldoSaida;
    }
}
