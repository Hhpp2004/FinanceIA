package financa.backend.Service;

import java.util.List;

import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import financa.backend.Exception.FinancasException;
import financa.backend.Exception.NotExistsExtrato;
import financa.backend.Exception.NotExistsUser;
import financa.backend.Model.Extrato;
import financa.backend.Model.Users;
import financa.backend.Repository.ExtratoRepository;
import financa.backend.Repository.UserRepository;

@Service
public class ExtratoService {
    private final ExtratoRepository er;
    private final UserRepository ur;
    
    public ExtratoService(ExtratoRepository er, UserRepository ur) {
        this.er = er;
        this.ur = ur;
    }

    private int id(JwtAuthenticationToken jwt) {
        String subject = jwt.getToken().getSubject();
        if (subject == null || !subject.matches("\\d+")) {
            throw new FinancasException();
        }
        int id = Integer.valueOf(subject);
        return id;
    }

    public Extrato extratoByName(String nome, JwtAuthenticationToken jwt) {
        int id = id(jwt);
        Users users = ur.findById(id).orElseThrow(NotExistsUser::new);
        var extratos = er.findByNomeAndUserOrderByDateDesc(nome, users);
        if (extratos.isEmpty()) {
            throw new NotExistsExtrato();
        }
        Extrato extrato = extratos.get(0);
        return extrato;
    }

    public List<Extrato> listAll(JwtAuthenticationToken jwt)  {
        int id = id(jwt);
        Users user = ur.findById(id).orElseThrow(NotExistsUser::new);
        List<Extrato> extratos = er.findByUser(user);
        if(extratos.isEmpty()){
            throw new NotExistsExtrato();
        } else {
            return extratos;
        }
    }
}