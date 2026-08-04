package financa.backend.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.List;

import org.springframework.scheduling.annotation.Async;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import financa.backend.DTO.OperacaoDTO;
import financa.backend.Exception.FinancasException;
import financa.backend.Exception.NotExistsExtrato;
import financa.backend.Exception.NotExistsOperacao;
import financa.backend.Exception.NotExistsUser;
import financa.backend.Exception.TimeData;
import financa.backend.Model.Extrato;
import financa.backend.Model.Operacao;
import financa.backend.Model.Saldo;
import financa.backend.Model.Tipo;
import financa.backend.Model.Users;
import financa.backend.Repository.ExtratoRepository;
import financa.backend.Repository.OperacaoRepository;
import financa.backend.Repository.SaldoRepository;
import financa.backend.Repository.UserRepository;
import jakarta.transaction.Transactional;

@Service
public class OperacaoService {
    private final OperacaoRepository or;
    private final ExtratoRepository er;
    private final SaldoRepository sr;
    private final UserRepository ur;

    public OperacaoService(OperacaoRepository or, ExtratoRepository er, SaldoRepository sr,
            UserRepository ur) {
        this.or = or;
        this.er = er;
        this.sr = sr;
        this.ur = ur;
    }

    private int id(JwtAuthenticationToken jwt) {
        String subject = jwt.getToken().getSubject();
        if (subject == null || !subject.matches("\\d+")) {
            throw new FinancasException();
        }
        int id = Integer.valueOf(subject);
        return id;
    }

    public List<Operacao> listAll(JwtAuthenticationToken jwt, String nome) {
        int id = id(jwt);
        Users user = ur.findById(id).orElseThrow(NotExistsUser::new);
        List<Extrato> extratos = er.findByNomeAndUserOrderByDateDesc(nome, user);
        if (extratos.isEmpty()) {
            throw new NotExistsExtrato();
        }
        Extrato extrato = extratos.get(0);
        List<Operacao> lista = or.findByExtrato(extrato);
        return lista;
    }

    public Operacao operacao(JwtAuthenticationToken jwt, String nome, String nomeExtrato)  {
        int id = id(jwt);
        Users user = ur.findById(id).orElseThrow(NotExistsUser::new);
        if (user != null) {
            Extrato extrato = er.findByNomeAndUser(nomeExtrato, user).orElseThrow(NotExistsExtrato::new);
            Operacao operacao = or.findByNomeAndExtrato(nome, extrato).orElseThrow(NotExistsOperacao::new);
            return operacao;
        } else {
            throw new NotExistsUser();
        }

    }

    public String formatter(LocalDate now) {
        DateTimeFormatter format = DateTimeFormatter.ofPattern("MMMM / yyyy", Locale.of("pt", "BR"));
        String nome = now.format(format);
        return nome.substring(0, 1).toUpperCase() + nome.substring(1);
    }

    @Async
    @Transactional
    public Extrato createExtrato(Users user, OperacaoDTO newOperacao) {
        Saldo saldo = new Saldo();
        if (newOperacao.tipo() == Tipo.ENTRADA) {
            saldo.setSaldoAtual(saldo.getSaldoAtual() + newOperacao.valor());
            saldo.setSaldoEntrada(saldo.getSaldoEntrada() + newOperacao.valor());
        } else if (newOperacao.tipo() == Tipo.SAIDA) {
            Double valor = 0.0;
            if (newOperacao.valor() < 0) {
                valor = newOperacao.valor() * (-1);
            } else {
                valor = newOperacao.valor();
            }
            saldo.setSaldoAtual(saldo.getSaldoAtual() - valor);
            saldo.setSaldoSaida(saldo.getSaldoSaida() + newOperacao.valor());
        }
        LocalDate date = localDatetoString(newOperacao.data());
        Extrato extrato = new Extrato(date, saldo, user);
        er.save(extrato);
        sr.save(saldo);
        return extrato;
    }
    
    @Transactional
    private void updateSaldo(Users user, OperacaoDTO newOperacao, boolean pertenceAoMesAtual) throws FinancasException {
        Saldo saldo = user.getSaldo();
        if (saldo != null) {
            if (newOperacao.tipo() == Tipo.ENTRADA) {
                saldo.setSaldoAtual(saldo.getSaldoAtual() + newOperacao.valor());
                if (pertenceAoMesAtual) {
                    saldo.setSaldoEntrada(saldo.getSaldoEntrada() + newOperacao.valor());
                }
            } else if (newOperacao.tipo() == Tipo.SAIDA) {
                Double valor = 0.0;
                if (newOperacao.valor() < 0) {
                    valor = newOperacao.valor() * (-1);
                } else {
                    valor = newOperacao.valor();
                }
                saldo.setSaldoAtual(saldo.getSaldoAtual() - valor);
                if (pertenceAoMesAtual) {
                    saldo.setSaldoSaida(saldo.getSaldoSaida() + valor);
                }
            }
            sr.save(saldo);
        } else {
            throw new FinancasException();
        }
    }

    @Transactional
    private void updateExtrato(Extrato extrato, OperacaoDTO newOperacao) throws FinancasException {
        Saldo saldo = extrato.getSaldo();
        if (saldo != null) {
            if (newOperacao.tipo() == Tipo.ENTRADA) {
                saldo.setSaldoAtual(saldo.getSaldoAtual() + newOperacao.valor());
                saldo.setSaldoEntrada(saldo.getSaldoEntrada() + newOperacao.valor());
            } else if (newOperacao.tipo() == Tipo.SAIDA) {
                Double valor = 0.0;
                if (newOperacao.valor() < 0) {
                    valor = newOperacao.valor() * (-1);
                }
                else {
                    valor = newOperacao.valor();
                }
                saldo.setSaldoAtual(saldo.getSaldoAtual() - valor);
                saldo.setSaldoSaida(saldo.getSaldoSaida() + newOperacao.valor());
            }
            extrato.setSaldo(saldo);
        } else {
            Saldo saldo2 = new Saldo();
            extrato.setSaldo(saldo2);
        }
        er.save(extrato);
    }

    LocalDate localDatetoString(String data) {
        if (data == null || data.isBlank()) {
            throw new FinancasException();
        }

        List<DateTimeFormatter> formatters = List.of(
                DateTimeFormatter.ofPattern("dd-MM-yyyy"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd"),
                DateTimeFormatter.ofPattern("dd/MM/yyyy"));

        for (DateTimeFormatter formatter : formatters) {
            try {
                return LocalDate.parse(data.trim(), formatter);
            } catch (DateTimeParseException ignored) {
                System.err.println("Erro de data");
            }
        }

        throw new FinancasException();
    }

    @Transactional
    public void createOperacao(JwtAuthenticationToken jwt, OperacaoDTO newOperacao)  {
        int id = id(jwt);
        var user = ur.findById(id).orElseThrow(NotExistsUser::new);
        createOperacao(user, newOperacao);
    }

    @Transactional
    public void createOperacao(Users user, OperacaoDTO newOperacao)  {
        LocalDate data = localDatetoString(newOperacao.data());
        if (data.isAfter(LocalDate.now())) {
            throw new TimeData();
        }
        String dataString = formatter(data);
        var extratos = er.findByNomeAndUserOrderByDateDesc(dataString, user);
        Extrato extrato;
        boolean pertenceAoMesAtual = YearMonth.from(data).equals(YearMonth.now());
        updateSaldo(user, newOperacao, pertenceAoMesAtual);
        if (!extratos.isEmpty()) {
            extrato = extratos.get(0);
            updateExtrato(extrato, newOperacao);
        } else {
            extrato = createExtrato(user, newOperacao);
        }
        Operacao operacao = new Operacao(newOperacao.nome(), newOperacao.descricao(), newOperacao.valor(),
                data.format(DateTimeFormatter.ofPattern("dd/MMMM/yyyy", Locale.of("pt", "BR"))),
                newOperacao.categoria(), newOperacao.tipo(), extrato, false);
        or.save(operacao);
    }    
}
