package financa.backend.Repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import financa.backend.Model.Saldo;

@Repository
public interface SaldoRepository extends JpaRepository<Saldo, UUID> {
    
}
