package financa.backend.Service;

import java.io.InputStream;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.stereotype.Service;

import financa.backend.DTO.GeradorPdfDTO;
import financa.backend.Exception.NotExistsExtrato;
import financa.backend.Model.Extrato;
import financa.backend.Model.Users;
import financa.backend.Repository.ExtratoRepository;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;

@Service
public class GenerateFilePDF {
    private static final String REPORT_PATH = "/reports/finance_model.jrxml";
    private static final Locale LOCALE_PT_BR = Locale.of("pt", "BR");

    private final DataSource dataSource;
    private final EmailService emailService;
    private final ExtratoRepository extratoRepository;

    public GenerateFilePDF(DataSource dataSource, EmailService emailService,
            ExtratoRepository extratoRepository) {
        this.dataSource = dataSource;
        this.emailService = emailService;
        this.extratoRepository = extratoRepository;
    }

    /** Gera o PDF exclusivamente em memória. */
    public byte[] generateFile(Users user, Extrato extrato) {
        validarAcesso(user, extrato);

        try (InputStream template = getClass().getResourceAsStream(REPORT_PATH);
                Connection connection = dataSource.getConnection()) {
            if (template == null) {
                throw new IllegalStateException("Modelo Jasper não encontrado: " + REPORT_PATH);
            }

            JasperReport report = JasperCompileManager.compileReport(template);
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("USER_ID", user.getId());
            parameters.put("EXTRATO_ID", extrato.getId().toString());
            parameters.put(JRParameter.REPORT_LOCALE, LOCALE_PT_BR);

            JasperPrint print = JasperFillManager.fillReport(report, parameters, connection);
            return JasperExportManager.exportReportToPdf(print);
        } catch (Exception exception) {
            throw new IllegalStateException("Não foi possível gerar o relatório PDF", exception);
        }
    }

    /** Gera em memória e envia; nenhum arquivo é gravado no computador/servidor. */
    public void generateAndSend(Users user, Extrato extrato) {
        byte[] pdf = generateFile(user, extrato);
        emailService.sendPdfReport(user, extrato.getNome(), pdf);
    }

    public byte[] generateFile(Users user, GeradorPdfDTO periodo) {
        return generateFile(user, findExtrato(user, periodo));
    }

    public void generateAndSend(Users user, GeradorPdfDTO periodo) {
        generateAndSend(user, findExtrato(user, periodo));
    }

    private Extrato findExtrato(Users user, GeradorPdfDTO periodo) {
        return extratoRepository.findByNomeAndUser(periodo.mes(), user)
                .orElseThrow(NotExistsExtrato::new);
    }

    private void validarAcesso(Users user, Extrato extrato) {
        if (extrato.getUser() == null || extrato.getUser().getId() != user.getId()) {
            throw new IllegalArgumentException("O extrato não pertence ao usuário autenticado");
        }
    }

}
