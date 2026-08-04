package financa.backend.Exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

public class FinancasException extends RuntimeException {
    public ProblemDetail toProblemDetails(){
        var pb = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        pb.setTitle("500 - Erro no servidor");
        return pb;
    }
}
