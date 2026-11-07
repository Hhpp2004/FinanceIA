package financa.backend.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import financa.backend.DTO.ConfirmacaoDTO;
import financa.backend.DTO.CreateUserDTO;
import financa.backend.DTO.LoginReq;
import financa.backend.DTO.LoginRes;
import financa.backend.DTO.SendFileAutoDTO;
import financa.backend.DTO.UpdateDTO;
import financa.backend.Exception.ErrorCreateUser;
import financa.backend.Exception.FinancasException;
import financa.backend.Exception.UserIsNotVerify;
import financa.backend.Model.Saldo;
import financa.backend.Model.Users;
import financa.backend.Service.UserService;
import jakarta.validation.Valid;



@Controller
public class UserController {
    private final UserService us;
    private final PasswordEncoder passwordEncoder;

    public UserController(UserService us, PasswordEncoder passwordEncoder) {
        this.us = us;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/cadastro")
    public ResponseEntity<String> createUser(@Valid @RequestBody CreateUserDTO newUser) throws ErrorCreateUser {
        Users user = new Users(newUser.nome(), newUser.telefone(), passwordEncoder.encode(newUser.senha()),
                newUser.email(), new Saldo(), false, null);
        us.createUser(user);
        return ResponseEntity.ok("201 - Usuario Criado, email enviado para ativação de conta");
    }
    
    @PostMapping("/confirmacao")
    public ResponseEntity<String> confirmacao(@Valid @RequestBody ConfirmacaoDTO confimar) throws UserIsNotVerify {
        us.validarCodigo(confimar.email(), confimar.codigo());
        return ResponseEntity.ok("201 - Conta confirmada");
    }

    @GetMapping("/user")
    public ResponseEntity<Users> user(JwtAuthenticationToken jwt) throws FinancasException {
        Users user = us.user(jwt);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/user")
    public ResponseEntity<String> updateUser(@Valid @RequestBody UpdateDTO updateUser, JwtAuthenticationToken jwt) {
        us.updateUser(jwt, updateUser);
        return ResponseEntity.ok("201 - Atualizado com sucesso");
    }

    @PutMapping("user/envio-automatico")
    public ResponseEntity<String> sendFileUpdate(JwtAuthenticationToken jwt, @Valid @RequestBody SendFileAutoDTO dto) {
        us.sendFileAuto(dto, jwt);
        return ResponseEntity.ok("200 - Dado Atualizado");
    }

    @PostMapping("/login")
    public ResponseEntity<LoginRes> login(@Valid @RequestBody LoginReq login) {
        LoginRes jwt = us.login(login);
        return ResponseEntity.ok(jwt);
    }    
}
