package TI4.OrtoBia.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import TI4.OrtoBia.models.Relatorio;

@Repository
public interface RelatorioRepository extends JpaRepository<Relatorio, Long> {
    List<Relatorio> findByUsuarioId(Long usuarioId);
}
