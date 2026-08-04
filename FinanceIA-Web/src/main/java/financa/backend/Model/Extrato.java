package financa.backend.Model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Extrato {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private String nome;
    private LocalDate date;
    @JoinColumn(name = "saldo_id")
    @OneToOne(cascade = CascadeType.ALL)
    private Saldo saldo;
    @JoinColumn(name = "user_id")
    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private Users user;
    
    private String formatter(LocalDate now)
    {
        DateTimeFormatter format = DateTimeFormatter.ofPattern("MMMM / yyyy", Locale.of("pt", "BR"));
        String nome = now.format(format);
        return nome.substring(0, 1).toUpperCase() + nome.substring(1);
    }

    public Extrato(LocalDate date, Saldo saldo, Users user) {
        this.nome = formatter(date);
        this.date = date;
        this.saldo = saldo;
        this.user = user;
    }
}
