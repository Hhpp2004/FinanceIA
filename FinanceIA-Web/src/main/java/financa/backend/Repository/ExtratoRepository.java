package financa.backend.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import financa.backend.Model.Extrato;
import financa.backend.Model.Users;

@Repository
public interface ExtratoRepository extends JpaRepository<Extrato, UUID> {
    List<Extrato> findByNomeAndUserOrderByDateDesc(String nome, Users user);

    List<Extrato> findByUser(Users user);

    List<Extrato> findByUserOrderByDateDesc(Users user);

    Optional<Extrato> findTopByUserOrderByDateDesc(Users user);

    boolean existsByNome(String nomeExtrato);
    
    Optional<Extrato> findByNomeAndUser(String nomeExtrato, Users user);

    Optional<Extrato> findByIdAndUser(UUID id, Users user);
}
