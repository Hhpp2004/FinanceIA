package financa.backend.Controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import financa.backend.Service.GenerateFilePDF;
import financa.backend.DTO.GeradorPdfDTO;
import financa.backend.Model.Extrato;
import financa.backend.Model.Users;
import financa.backend.Service.ExtratoService;
import financa.backend.Service.UserService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@Controller
public class ExtratoController {
    private final ExtratoService es;
    private final GenerateFilePDF pdf;
    private final UserService us;

    @GetMapping("/file")
    public ResponseEntity<String> sendFile(JwtAuthenticationToken user,@Valid @RequestBody GeradorPdfDTO nome) {
        Users user2 = us.user(user);
        pdf.generateAndSend(user2, nome);
        return ResponseEntity.ok("Email enviado");
    }

    @GetMapping("/extrato")
    public ResponseEntity<List<Extrato>> listAll(JwtAuthenticationToken user) {
        List<Extrato> extratos = es.listAll(user);
        return ResponseEntity.ok(extratos);
    }

    @GetMapping(value = "/extrato", params = "nome")
    public ResponseEntity<Extrato> extratoByName(@Valid @RequestParam String nome, JwtAuthenticationToken jwt)
    {
        Extrato extrato = es.extratoByName(nome, jwt);
        return ResponseEntity.ok(extrato);
    }
}
