package financa.backend.Service;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.InputStream;

import org.junit.jupiter.api.Test;

import net.sf.jasperreports.engine.JasperCompileManager;

class GenerateFilePDFTest {

    @Test
    void deveCompilarModeloDoRelatorio() throws Exception {
        try (InputStream template = getClass().getResourceAsStream("/reports/finance_model.jrxml")) {
            assertNotNull(template, "Modelo Jasper não encontrado");
            assertNotNull(JasperCompileManager.compileReport(template));
        }
    }
}
