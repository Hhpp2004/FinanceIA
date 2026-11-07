package financa.backend.Controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import financa.backend.DTO.OperacaoDTO;
import financa.backend.Model.Operacao;
import financa.backend.Service.OperacaoService;
import jakarta.validation.Valid;

@Controller
public class OperacaoController {

    private final OperacaoService os;

    public OperacaoController(OperacaoService os) {
        this.os = os;
    }

    @GetMapping(value = "/operacao", params = "extrato")
    public ResponseEntity<List<Operacao>> listAll(@Valid @RequestParam String extrato, JwtAuthenticationToken jwt) {
        List<Operacao> lista = os.listAll(jwt, extrato);
        return ResponseEntity.ok(lista);
    }

    @GetMapping(value = "/operacao", params = { "extrato", "operacao" })
    public ResponseEntity<Operacao> operacaoById(@Valid @RequestParam String extrato, @Valid @RequestParam String operacao, JwtAuthenticationToken jwt) {
        Operacao operacao2 = os.operacao(jwt, operacao, extrato);
        return ResponseEntity.ok(operacao2);
    }

    @PostMapping("/operacao")
    public ResponseEntity<String> createOperacao(@Valid @RequestBody OperacaoDTO newOperacao,
            JwtAuthenticationToken user)  {
        os.createOperacao(user, newOperacao);
        return ResponseEntity.ok("201 - Operacao Adiconado");
    }
}
