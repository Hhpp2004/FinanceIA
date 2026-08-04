package financa.backend.Service;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import financa.backend.DTO.EmailDTO;
import financa.backend.Exception.NotExistsUser;
import financa.backend.Model.Users;
import financa.backend.Producer.FinancerProducer;
import financa.backend.Repository.UserRepository;

@Service
public class EmailService {
    private final FinancerProducer financerProducer;
    private final UserRepository userRepository;

    public EmailService(FinancerProducer financerProducer, UserRepository userRepository) {
        this.financerProducer = financerProducer;
        this.userRepository = userRepository;
    }

    public void sendActivationCode(Users user) {
        EmailDTO dto = new EmailDTO();
        dto.setUserId(user.getId());
        dto.setNome(user.getNome());
        dto.setCode(user.getCode());
        dto.setFilePDFExists(false);
        dto.setEmailTo(user.getEmail());
        dto.setSubject("Confirmação de email");
        dto.setText("Olá senhor(a) " + user.getNome()
                + ", por favor confirme o seu e-mail logo abaixo.\nDigite esse codigo no aplicativo para confimar o seu e-mail: "
                + user.getCode() + "\nLembrete esse codigo é valido por 10 minutos.");
        financerProducer.publishMessageEmail(dto);
    }

    public void sendEmailError(String email){
        EmailDTO dto = new EmailDTO();
        Users user = userRepository.findByEmail(email).orElseThrow();
        dto.setEmailTo(email);
        dto.setSubject("Atenção - Muitas tentativas de acesso");
        dto.setUserId(user.getId());
        dto.setText("Olá senhor(a) "+user.getNome()+", houve muitas tentativas de acesso a sua conta sem sucesso. Caso não foi o senhor(a) entre em contato ao suporte");
        financerProducer.publishMessageEmail(dto);
    }

    public void sendPdfReport(Users user, String periodo, byte[] pdfBytes) {
        EmailDTO dto = new EmailDTO();
        dto.setUserId(user.getId());
        dto.setEmailTo(user.getEmail());
        dto.setNome(user.getNome());
        dto.setSubject("Relatório financeiro - " + periodo);
        dto.setText("Olá, " + user.getNome()
                + ".\n\nSegue em anexo o relatório financeiro referente a " + periodo + ".");
        dto.setFilePDF(pdfBytes);
        dto.setFilePDFExists(true);
        financerProducer.publishMessageEmail(dto);
    }

    @Async
    public void emailConfirmado(String email) {
        Users user = userRepository.findByEmail(email).orElseThrow(NotExistsUser::new);
        EmailDTO dto = new EmailDTO();
        dto.setUserId(user.getId());
        dto.setEmailTo(email);
        dto.setFilePDFExists(false);
        dto.setNome(user.getNome());
        dto.setSubject("Email confirmado");
        dto.setText("Seja bem vindo senhor(a) " + user.getNome()
        + ". Bom saber que seu email foi confirmado, aproveite a nossa plataforma.");
        financerProducer.publishMessageEmail(dto);
    }

    @Async
    public void sendPasswordResetCode(Users user) {
        EmailDTO dto = new EmailDTO();
        dto.setUserId(user.getId());
        dto.setEmailTo(user.getEmail());
        dto.setNome(user.getNome());
        dto.setFilePDFExists(false);
        dto.setSubject("Código de recuperação de senha");
        dto.setText("Olá, " + user.getNome() + ".\n\nSeu código de recuperação é: "
                + user.getCode()
                + "\n\nO código é válido por 10 minutos. Se você não solicitou a recuperação, ignore este e-mail.");
        financerProducer.publishMessageEmail(dto);
    }
}
