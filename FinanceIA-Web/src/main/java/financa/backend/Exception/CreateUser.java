package financa.backend.Exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

public class CreateUser extends FinancasException {
    @Override
    public ProblemDetail toProblemDetails() {
        var pb = ProblemDetail.forStatus(HttpStatus.CREATED);
        pb.setDetail("200 - Usuario Criado");
        return pb;
    }
}
