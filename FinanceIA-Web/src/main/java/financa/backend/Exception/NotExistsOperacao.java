package financa.backend.Exception;

import org.springframework.http.ProblemDetail;

public class NotExistsOperacao extends FinancasException {
    @Override
    public ProblemDetail toProblemDetails() {
        var pb = ProblemDetail.forStatus(404);
        pb.setDetail("404 - Operação não encontrado");
        return pb;
    }
}
