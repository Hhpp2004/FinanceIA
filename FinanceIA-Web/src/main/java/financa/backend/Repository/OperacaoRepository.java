package financa.backend.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.time.LocalDate;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import financa.backend.Model.Extrato;
import financa.backend.Model.Operacao;
import financa.backend.Model.Users;

@Repository
public interface OperacaoRepository extends JpaRepository<Operacao, UUID> {
    List<Operacao> findByExtrato(Extrato extrato);

    List<Operacao> findByExtratoUserAndDataBetween(Users user, LocalDate inicio, LocalDate fim);

    Optional<Operacao> findByNomeAndExtrato(String nome, Extrato extrato);

    Optional<Operacao> findByIdAndExtrato(UUID id, Extrato extrato);
}
