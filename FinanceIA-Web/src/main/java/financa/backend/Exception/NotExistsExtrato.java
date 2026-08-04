package financa.backend.Exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

public class NotExistsExtrato extends FinancasException {
    @Override
    public ProblemDetail toProblemDetails() {
        var pb = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        pb.setDetail("404 - Extrato não encontrado");
        return pb;
    }
}
