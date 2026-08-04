package financa.backend.Exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;


public class TimeData extends FinancasException {
    @Override
    public ProblemDetail toProblemDetails()
    {
        var pb = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        pb.setTitle("Data inválida");
        pb.setDetail("Não é possível cadastrar uma operação com data futura.");
        return pb;
    }
}
