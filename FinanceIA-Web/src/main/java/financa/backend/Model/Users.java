package financa.backend.Model;

import java.time.LocalTime;

import org.springframework.security.crypto.password.PasswordEncoder;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import financa.backend.DTO.LoginReq;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Setter
@Getter
@ToString
@NoArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Users {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String nome;
    private String telefone;
    @JsonIgnore
    private String senha;
    @Column(unique = true)
    private String email;
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private Agendamento Agendamento = new Agendamento();
    @JoinColumn(name = "saldo_id")
    @OneToOne(cascade = CascadeType.ALL)
    private Saldo saldo;
    private boolean isVerify;
    private boolean isSendAutoFile;
    private int sendFile;
    @JsonIgnore
    private String code;
    @JsonIgnore
    private LocalTime ExpireAt;

    public Users(String nome, String telefone, String senha, String email,
            financa.backend.Model.Agendamento agendamento, Saldo saldo, boolean isVerify, String code,
            LocalTime expireAt) {
        this.nome = nome;
        this.telefone = telefone;
        this.senha = senha;
        this.email = email;
        Agendamento = agendamento;
        this.saldo = saldo;
        this.isVerify = isVerify;
        this.isSendAutoFile = false;
        this.code = code;
        ExpireAt = expireAt;
    }

    public Users(String nome, String telefone, String senha, String email, Saldo saldo,
            boolean isVerify, LocalTime expireAt) {
        this.nome = nome;
        this.telefone = telefone;
        this.senha = senha;
        this.email = email;
        this.saldo = saldo;
        this.isVerify = false;
        this.isSendAutoFile = false;
        this.code = null;
        ExpireAt = expireAt;
    }

    public boolean isloginCorrect(PasswordEncoder password, LoginReq senha) {
        return password.matches(senha.senha(), this.senha);
    }
}
