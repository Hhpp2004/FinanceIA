package financa.backend.Exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

public class ErrorCreateSaldo extends FinancasException{
    @Override
    public ProblemDetail toProblemDetails() {
        var pb = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        pb.setDetail("400 - Erro ao criar o Saldo do usuario");
        return pb;
    }
}
