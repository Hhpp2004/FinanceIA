package financa.backend.Exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

public class UserExists extends FinancasException {
    @Override
    public ProblemDetail toProblemDetails() {
        var pb = ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED);
        pb.setDetail("401 - Email já em uso");
        return pb;
    }
}
