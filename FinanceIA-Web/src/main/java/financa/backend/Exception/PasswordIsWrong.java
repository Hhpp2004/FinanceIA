package financa.backend.Exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

public class PasswordIsWrong extends FinancasException{
    @Override
    public ProblemDetail toProblemDetails() {
        var pb = ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED);
        pb.setTitle("401 - Senha errada");
        return pb;
    }
}
