package financa.backend.Exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

public class UserIsNotVerify extends FinancasException {
    @Override
    public ProblemDetail toProblemDetails() {
        ProblemDetail pb = ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED);
        pb.setDetail("401 - Usuario com email não validado");
        return pb;
    }
}
