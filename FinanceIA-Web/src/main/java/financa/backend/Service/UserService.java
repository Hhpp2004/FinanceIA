package financa.backend.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import financa.backend.DTO.GeradorPdfDTO;
import financa.backend.DTO.LoginReq;
import financa.backend.DTO.LoginRes;
import financa.backend.DTO.SendFileAutoDTO;
import financa.backend.DTO.UpdateDTO;
import financa.backend.Exception.ErrorCreateSaldo;
import financa.backend.Exception.ErrorCreateUser;
import financa.backend.Exception.FinancasException;
import financa.backend.Exception.NotExistsUser;
import financa.backend.Exception.PasswordIsWrong;
import financa.backend.Exception.UserExists;
import financa.backend.Exception.UserIsNotVerify;
import financa.backend.Model.Users;
import financa.backend.Repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@Service
public class UserService {
    private final JwtEncoder jwt;
    private final UserRepository ur;
    private final BCryptPasswordEncoder passwordEncoder;
    private final EmailService es;
    private final GenerateFilePDF pdf;
    private final Random random;

    private int id(JwtAuthenticationToken jwt) {
        String subject = jwt.getToken().getSubject();
        if (subject == null || !subject.matches("\\d+")) {
            throw new FinancasException();
        }
        int id = Integer.parseInt(subject);
        return id;
    }

    @Transactional
    public void updateUser(JwtAuthenticationToken jwt, UpdateDTO update) {
        int id = id(jwt);
        Users user = ur.findById(id).orElseThrow(NotExistsUser::new);
        if (user != null) {
            if (update.nome() != null) {
                user.setNome(update.nome());
            } else if (update.senha() != null) {
                user.setSenha(passwordEncoder.encode(update.senha()));
            } else if (update.email() != null && !ur.existsByEmail(update.email())) {
                user.setEmail(update.email());
            } else if (ur.existsByEmail(update.email())) {
                throw new UserExists();
            } else if (update.telefone() != null) {
                user.setTelefone(update.telefone());
            }
            ur.save(user);
        }
    }

    public Users user(JwtAuthenticationToken jwt) {
        int id = id(jwt);
        Users user = ur.findById(id).orElseThrow(NotExistsUser::new);
        return user;
    }

    @Transactional
    public LoginRes login(LoginReq req) {
        Users user = ur.findByEmail(req.email()).orElseThrow(NotExistsUser::new);
        if (user.isloginCorrect(passwordEncoder, req)) {
            if (user.isVerify()) {
                Instant now = Instant.now();
                Long timeExpireAt = 3600l;
                var clains = JwtClaimsSet.builder().issuer("Security").subject(user.getId() + "")
                        .issuedAt(now).expiresAt(now.plusSeconds(timeExpireAt)).build();
                String jwtvalues = jwt.encode(JwtEncoderParameters.from(clains)).getTokenValue();
                sendFile(user);
                return new LoginRes(jwtvalues, timeExpireAt);
            } else {
                throw new UserIsNotVerify();
            }
        } else {
            throw new PasswordIsWrong();
        }
    }

    public void afterSuccessfulLogin(String email) {
        Users user = ur.findByEmail(email).orElseThrow(NotExistsUser::new);
        sendFile(user);
    }

    public void requestPasswordReset(String email) {
        ur.findByEmail(email).filter(Users::isVerify).ifPresent(user -> {
            user.setCode(gerarCodigo());
            user.setExpireAt(java.time.LocalTime.now().plusMinutes(10));
            ur.save(user);
            es.sendPasswordResetCode(user);
        });
    }

    public boolean isValidPasswordResetCode(String email, String code) {
        return ur.findByEmail(email)
                .filter(Users::isVerify)
                .filter(user -> user.getCode() != null && user.getCode().equals(code))
                .filter(user -> user.getExpireAt() != null && java.time.LocalTime.now().isBefore(user.getExpireAt()))
                .isPresent();
    }

    @Transactional
    public boolean resetPassword(String email, String code, String newPassword) {
        Users user = ur.findByEmail(email).orElse(null);
        if (user == null || !user.isVerify() || user.getCode() == null
                || !user.getCode().equals(code) || user.getExpireAt() == null
                || !java.time.LocalTime.now().isBefore(user.getExpireAt())) {
            return false;
        }
        user.setSenha(passwordEncoder.encode(newPassword));
        user.setCode(null);
        user.setExpireAt(null);
        ur.save(user);
        return true;
    }

    public String gerarCodigo() {
        return String.valueOf(100000 + random.nextInt(900000));
    }

    public boolean validarCodigo(String email, String codigo) {
        Users user = ur.findByEmail(email).orElseThrow(NotExistsUser::new);
        if (user == null || user.isVerify()) {
            return false;
        }
        if (user.getCode() == null || !user.getCode().equals(codigo)
                || user.getExpireAt() == null
                || !LocalTime.now().isBefore(user.getExpireAt())) {
            return false;
        }
        user.setCode(null);
        user.setExpireAt(null);
        user.setVerify(true);
        ur.save(user);
        es.emailConfirmado(email);
        return true;
    }

    public String getMonth(LocalDate now) {
        DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedPattern("MMMM");
        String mes = now.format(formatter);
        return mes;
    }

    public void sendFile(Users user) {
        LocalDate day = LocalDate.ofEpochDay(user.getSendFile());
        if (user.isSendAutoFile() == true) {
            if (LocalDate.now().equals(day)) {
                LocalDate aux = LocalDate.now();
                GeradorPdfDTO month = new GeradorPdfDTO(getMonth(aux));
                pdf.generateAndSend(user, month);
            }
        }
    } 

    public void sendFileAuto(SendFileAutoDTO dto, JwtAuthenticationToken jwt) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd / MMMM / yyyy");
        LocalDate day = LocalDate.parse(dto.dia(), formatter);
        int id = id(jwt);
        Users user = ur.findById(id).orElseThrow(NotExistsUser::new);
        if (user.isSendAutoFile() == true) {
            user.setSendAutoFile(false);
            user.setSendFile(0);
        }
        else {
            user.setSendAutoFile(true);
            user.setSendFile(day.getDayOfMonth());
        }
        ur.save(user);
    }

    @Transactional
    public void createUser(Users newUser) {
        if (newUser == null) {
            throw new ErrorCreateUser();
        }
        if (newUser.getSaldo() == null) {
            throw new ErrorCreateSaldo();
        }
        if (ur.existsByEmail(newUser.getEmail())) {
            throw new UserExists();
        }

        newUser.setCode(gerarCodigo());
        newUser.setExpireAt(LocalTime.now().plusMinutes(10));
        ur.saveAndFlush(newUser);
        es.sendActivationCode(newUser);
    }
}
